package hu.akarnokd.reactive.lowalloc;

import io.reactivex.Observer;

final class LcObservableRange extends LcSourceObservable<Integer> {

    final int start;

    final int end;

    volatile boolean disposed;

    LcObservableRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected LcSourceObservable<Integer> createFresh() {
        return new LcObservableRange(start, end);
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
    protected void subscribeActual(Observer<? super Integer> downstream) {
        downstream.onSubscribe(this);

        for (int i = start; i < end; i++) {
            if (disposed) {
                return;
            }
            downstream.onNext(i);
        }
        if (disposed) {
            return;
        }
        downstream.onComplete();
    }

}
