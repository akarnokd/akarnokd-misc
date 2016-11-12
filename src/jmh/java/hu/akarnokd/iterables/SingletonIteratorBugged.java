package hu.akarnokd.iterables;

import java.util.*;
import java.util.function.Supplier;

@FunctionalInterface
public interface SingletonIteratorBugged<E> extends Iterator<E>, Supplier<Supplier<E>[]> {

    @SuppressWarnings("unchecked")
    static <E> SingletonIteratorBugged<E> of(E e) {
        return () -> new Supplier[] { () -> e };
    }
    
    static <E> Supplier<E> none() {
        return () -> { throw new NoSuchElementException(); };
    }
    
    @Override
    default boolean hasNext() {
        return get()[0] == none();
    }
    
    @Override
    default E next() {
        Supplier<E> current = get()[0];
        get()[0] = none();
        return current.get();
    }
}
