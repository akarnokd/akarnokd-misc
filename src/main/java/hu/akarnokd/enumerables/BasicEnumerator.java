package hu.akarnokd.enumerables;

abstract class BasicEnumerator<T> implements IEnumerator<T> {

    protected T value;

    @Override
    public final T current() {
        return value;
    }
}
