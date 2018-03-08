package hu.akarnokd.rxjava2;

import java.util.concurrent.atomic.AtomicReferenceArray;

import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.*;

/**
 * A disposable container with a fixed number of slots that
 * limits the maximum number of Disposables that can be added
 * to it at a given time.
 */
public final class FixedCompositeDisposable
extends AtomicReferenceArray<Disposable>
implements DisposableContainer, Disposable {

    private static final long serialVersionUID = 4223876873450175402L;

    public FixedCompositeDisposable(int count) {
        super(count);
    }

    @Override
    public void dispose() {
        for (int i = 0; i < length(); i++) {
            Disposable d = getAndSet(i, DisposableHelper.DISPOSED);
            if (d != null && d != DisposableHelper.DISPOSED) {
                d.dispose();
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return get(0) == DisposableHelper.DISPOSED;
    }

    @Override
    public boolean add(Disposable d) {
        for (int i = 0; i < length(); i++) {
            if (get(i) == DisposableHelper.DISPOSED) {
                d.dispose();
                return false;
            }
            if (get(i) == null && compareAndSet(i, null, d)) {
                return true;
            }
        }
        if (get(0) == DisposableHelper.DISPOSED) {
            d.dispose();
            return false;
        }
        throw new IllegalStateException("FixedCompositeDisposable is full!");
    }

    @Override
    public boolean remove(Disposable d) {
        for (int i = 0; i < length(); i++) {
            if (get(i) == d) {
                compareAndSet(i, d, null);
                d.dispose();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(Disposable d) {
        for (int i = 0; i < length(); i++) {
            if (get(i) == d) {
                compareAndSet(i, d, null);
                return true;
            }
        }
        return false;
    }
}
