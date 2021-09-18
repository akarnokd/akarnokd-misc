package hu.akarnokd.rxjava2;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.*;

import org.reactivestreams.*;

import io.reactivex.Flowable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.subscriptions.*;

/**
 * Sums the numerical values of multiple source publishers as integer.
 * @param <T> the numerical input type
 */
public final class FlowableSumIntArray<T extends Number> extends Flowable<Integer> {

    final Publisher<T>[] sources;

    @SafeVarargs
    public FlowableSumIntArray(Publisher<T>... sources) {
        this.sources = sources;
    }

    @Override
    protected void subscribeActual(Subscriber<? super Integer> s) {
        SumIntArraySubscriber<T> parent = new SumIntArraySubscriber<>(s, sources);
        s.onSubscribe(parent);
        parent.onComplete();
    }

    static final class SumIntArraySubscriber<T extends Number> extends DeferredScalarSubscription<Integer>
    implements Subscriber<T> {

        private static final long serialVersionUID = 2372122834726771627L;

        final Publisher<T>[] sources;

        final AtomicInteger wip;

        final AtomicReference<Subscription> current;

        int index;

        boolean hasValue;
        int sum;

        SumIntArraySubscriber(Subscriber<? super Integer> actual, Publisher<T>[] sources) {
            super(actual);
            this.sources = sources;
            this.wip = new AtomicInteger();
            this.current = new AtomicReference<>();

        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.replace(current, s)) {
                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(T t) {
            if (!hasValue) {
                hasValue = true;
            }
            sum += t.intValue();
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void cancel() {
            super.cancel();
            SubscriptionHelper.cancel(current);
        }

        @Override
        public void onComplete() {
            if (wip.getAndIncrement() == 0) {
                Publisher<T>[] srcs = sources;
                int n = srcs.length;
                do {
                    if (isCancelled()) {
                        return;
                    }
                    int i = index;
                    if (i == n) {
                        if (hasValue) {
                            complete(sum);
                        } else {
                            downstream.onComplete();
                        }

                        return;
                    }
                    index = i + 1;
                    Publisher<T> p = srcs[i];
                    if (p instanceof Callable) {
                        @SuppressWarnings("unchecked")
                        Callable<T> callable = (Callable<T>) p;

                        T v;

                        try {
                            v = callable.call();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            downstream.onError(ex);
                            return;
                        }

                        if (v != null) {
                            hasValue = true;
                            sum += v.intValue();
                        }
                    } else {
                        p.subscribe(this);
                    }
                } while (wip.decrementAndGet() != 0);
            }
        }
    }
}
