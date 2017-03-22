package hu.akarnokd.reactive.observables;

public final class BooleanDisposable implements IDisposable {

    boolean disposed;

    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
    }
}
