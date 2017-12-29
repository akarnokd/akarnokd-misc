package hu.akarnokd.iterables;

import java.util.*;
import java.util.function.Supplier;

@FunctionalInterface
public interface SingletonSetBugged<E> extends Supplier<E>, Set<E> {

    static <E> SingletonSetBugged<E> of(E e) {
        return () -> e;
    }

    @Override
    default boolean add(E e) {
        return false;
    }

    @Override
    default boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    default void clear() {
    }

    @Override
    default boolean contains(Object o) {
        return Objects.equals(get(), o);
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(e -> Objects.equals(get(), e));
    }

    @Override
    default boolean isEmpty() {
        return false;
    }

    @Override
    default Iterator<E> iterator() {
        return SingletonIteratorBugged.of(get());
    }

    @Override
    default boolean remove(Object o) {
        return false;
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    default int size() {
        return 1;
    }

    @Override
    default Object[] toArray() {
        return new Object[] { get() };
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T> T[] toArray(T[] a) {
        return (T[])Arrays.copyOf(toArray(), 1, a.getClass());
    }
}
