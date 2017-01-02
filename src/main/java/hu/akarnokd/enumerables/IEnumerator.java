package hu.akarnokd.enumerables;

public interface IEnumerator<T> {

    boolean moveNext();

    T current();
}
