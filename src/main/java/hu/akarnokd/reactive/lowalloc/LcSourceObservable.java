package hu.akarnokd.reactive.lowalloc;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class LcSourceObservable<T> extends LcObservable<T> implements Disposable {

    volatile int sourceState;

    @SuppressWarnings("rawtypes")
    static final AtomicIntegerFieldUpdater<LcSourceObservable> SOURCE_STATE =
        AtomicIntegerFieldUpdater.newUpdater(LcSourceObservable.class, "sourceState");

    protected abstract void subscribeActual(Observer<? super T> downstream);

    protected abstract LcSourceObservable<T> createFresh();

    @Override
    public final void subscribe(Observer<? super T> observer) {
        if (sourceState == 0 && SOURCE_STATE.compareAndSet(this, 0, 1)) {
            subscribeActual(observer);
        } else {
            createFresh().subscribeActual(observer);
        }
    }
}
