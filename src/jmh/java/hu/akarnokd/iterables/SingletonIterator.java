package hu.akarnokd.iterables;

import java.util.*;
import java.util.function.Supplier;

@FunctionalInterface
public interface SingletonIterator<E> extends Iterator<E>, Supplier<Supplier<E>[]> {

    @SuppressWarnings("unchecked")
    static <E> SingletonIterator<E> of(E e) {
        Supplier<E>[] a = new Supplier[] { () -> e };
        return () -> a;
    }
    
    static <E> Supplier<E> none() {
        return () -> { throw new NoSuchElementException(); };
    }
    
    @Override
    default boolean hasNext() {
        return get()[0] != none();
    }
    
    @Override
    default E next() {
        Supplier<E> current = get()[0];
        get()[0] = none();
        return current.get();
    }
}
