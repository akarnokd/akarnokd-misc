package hu.akarnokd.reactive.lowalloc;

import io.reactivex.Observer;

final class LcObservableJust<T> extends LcSourceObservable<T> {

    final T value;

    volatile boolean disposed;

    LcObservableJust(T value) {
        this.value = value;
    }

    @Override
    protected LcSourceObservable<T> createFresh() {
        return new LcObservableJust<T>(value);
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    protected void subscribeActual(Observer<? super T> downstream) {
        downstream.onSubscribe(this);

        if (disposed) {
            return;
        }
        downstream.onNext(value);
        if (disposed) {
            return;
        }
        downstream.onComplete();
    }

}
