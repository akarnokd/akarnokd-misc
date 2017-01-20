package hu.akarnokd.enumerables;

import java.util.*;
import java.util.function.*;

public interface IEnumerable<T> {

    IEnumerator<T> enumerator();

    static <T> IEnumerable<T> fromIterable(Iterable<T> source) {
        return new IEFromIterable<>(source);
    }

    static IEnumerable<Integer> characters(CharSequence cs) {
        return new IEFromCharSequence(cs);
    }

    default <C> IEnumerable<C> collect(Supplier<C> supplier, BiConsumer<C, T> collector) {
        return new IECollect<>(this, supplier, collector);
    }

    default <R> IEnumerable<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return new IEFlatMapIterable<>(this, mapper);
    }

    default <R> IEnumerable<R> map(Function<? super T, ? extends R> mapper) {
        return new IEMap<>(this, mapper);
    }

    default IEnumerable<T> take(int n) {
        return new IETake<>(this, n);
    }

    default IEnumerable<T> skip(int n) {
        return new IESkip<>(this, n);
    }

    static <T> IEnumerable<T> just(T item) {
        return new IEJust<>(item);
    }

    default IEnumerable<T> filter(Predicate<? super T> predicate) {
        return new IEFilter<>(this, predicate);
    }

    default IEnumerable<Long> sumLong() {
        return new IESumLong<>(this);
    }

    default IEnumerable<Integer> sumInt() {
        return new IESumInt<>(this);
    }

    default IEnumerable<Integer> maxInt() {
        return new IEMaxInt<>(this);
    }

    @SafeVarargs
    static <T> IEnumerable<T> concatArray(IEnumerable<? extends T>... sources) {
        return new IEConcatArray<>(sources);
    }

    default T first() {
        IEnumerator<T> en = enumerator();
        if (en.moveNext()) {
            return en.current();
        }
        throw new NoSuchElementException();
    }

    default void assertResult(@SuppressWarnings("unchecked") T... values) {
        IEnumerator<T> en = enumerator();
        if (en.moveNext()) {
            for (int i = 0; i < values.length; i++) {
                T o1 = values[i];
                T o2 = en.current();
                if (!Objects.equals(o1, o2)) {
                    throw new AssertionError("Index: " + i + ", Expected: " + o1 + ", Actual: " + o2);
                }

                if (!en.moveNext()) {
                    if (i != values.length - 1) {
                        throw new AssertionError("The IEnumerator is shorter (" + i + ")");
                    }
                }
            }
            if (en.moveNext()) {
                throw new AssertionError("The IEnumerable is longer (" + values.length + ")");
            }
        } else {
            if (values.length != 0) {
                throw new AssertionError("The IEnumerator is empty");
            }
        }
    }
}
