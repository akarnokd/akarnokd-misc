package hu.akarnokd.rxjava2;

import org.reactivestreams.Subscription;

import reactor.core.CoreSubscriber;

public abstract class IntDeferredReducerCore extends DeferredScalarSubscriber<Integer, Integer>
implements CoreSubscriber<Integer> {

    Subscription s;

    int accumulator;

    boolean hasValue;

    public IntDeferredReducerCore(CoreSubscriber<? super Integer> actual) {
        super(actual);
    }

    @Override
    public final void onSubscribe(Subscription s) {
        this.s = s;

        subscriber.onSubscribe(this);

        s.request(Long.MAX_VALUE);
    }

    @Override
    public final void onError(Throwable t) {
        subscriber.onError(t);
    }

    @Override
    public final void onComplete() {
        if (hasValue) {
            complete(accumulator);
        } else {
            subscriber.onComplete();
        }
    }
}
