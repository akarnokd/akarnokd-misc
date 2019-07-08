package hu.akarnokd.reactive.lowalloc;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import io.reactivex.Observer;

public abstract class LcIntermediateObservable<Upstream, Downstream> extends LcObservable<Downstream> implements Observer<Upstream> {

    volatile int observerState;

    @SuppressWarnings("rawtypes")
    static final AtomicIntegerFieldUpdater<LcIntermediateObservable> OBSERVER_STATE =
        AtomicIntegerFieldUpdater.newUpdater(LcIntermediateObservable.class, "observerState");

    Observer<? super Downstream> downstream;

    protected abstract void subscribeActual();

    protected abstract LcIntermediateObservable<Upstream, Downstream> createFresh();

    @Override
    public final void subscribe(Observer<? super Downstream> observer) {
        // TODO Auto-generated method stub
        if (observerState == 0 && OBSERVER_STATE.compareAndSet(this, 0, 1)) {
            this.downstream = observer;
            subscribeActual();
        } else {
            LcIntermediateObservable<Upstream, Downstream> fresh = createFresh();
            fresh.downstream = observer;
            fresh.subscribeActual();
        }
    }
}
