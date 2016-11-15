package hu.akarnokd.rxjava2;

import org.reactivestreams.*;

public abstract class LongDeferredReducerCore extends DeferredScalarSubscriber<Long, Long>
implements Subscriber<Long> {

    Subscription s;

    long accumulator;

    boolean hasValue;

    public LongDeferredReducerCore(Subscriber<? super Long> actual) {
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
