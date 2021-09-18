package hu.akarnokd.rxjava2;

import io.reactivex.disposables.*;
import io.reactivex.internal.disposables.DisposableContainer;
import io.reactivex.internal.util.Pow2;

/**
 * A composite disposable that contains a power-of-two sub composites (stripes) and
 * Disposables are added to a composite based on its hashCode modulo the number of stripes.
 * <p>
 * This should spread the lock contention when there are a lot of adds/removes.
 */
public final class StripedCompositeDisposable implements DisposableContainer, Disposable {

    final CompositeDisposable[] stripes;

    StripedCompositeDisposable(int stripeCount) {
        this.stripes = new CompositeDisposable[Pow2.roundToPowerOfTwo(stripeCount)];
        for (int i = 0; i < stripes.length; i++) {
            this.stripes[i] = new CompositeDisposable();
        }
    }

    @Override
    public void dispose() {
        for (CompositeDisposable cd : stripes) {
            cd.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return stripes[0].isDisposed();
    }

    @Override
    public boolean add(Disposable d) {
        int n = stripes.length - 1;
        CompositeDisposable cd = stripes[d.hashCode() & n];
        return cd.add(d);
    }

    @Override
    public boolean remove(Disposable d) {
        int n = stripes.length - 1;
        CompositeDisposable cd = stripes[d.hashCode() & n];
        return cd.remove(d);
    }

    @Override
    public boolean delete(Disposable d) {
        int n = stripes.length - 1;
        CompositeDisposable cd = stripes[d.hashCode() & n];
        return cd.delete(d);
    }
}
