package hu.akarnokd.comparison;

import java.util.*;
import java.util.function.*;

import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;

public enum FluentIterables {
    
    ;
    
    static FluentIterable<Integer> sumInt(FluentIterable<Integer> source) {
        return reduce(source, () -> 0, (a, b) -> a + b);
    }

    static FluentIterable<Integer> maxInt(FluentIterable<Integer> source) {
        return reduce(source, () -> 0, Integer::max);
    }

    static FluentIterable<Long> maxLong(FluentIterable<Long> source) {
        return reduce(source, () -> 0L, Long::max);
    }

    static FluentIterable<Long> sumLong(FluentIterable<Long> source) {
        return reduce(source, () -> 0L, (a, b) -> a + b);
    }

    static <T, R> FluentIterable<R> collect(FluentIterable<T> source, Supplier<R> supplier, BiConsumer<R, T> consumer) {
        return reduce(source, supplier, (a, b) -> { consumer.accept(a, b); return a; });
    }

    static <T, R> FluentIterable<R> reduce(FluentIterable<T> source, Supplier<R> collectionSupplier, BiFunction<R, T, R> function) {
        return new FluentIterable<R>() {
            @Override
            public Iterator<R> iterator() {
                Iterator<T> it = source.iterator();
                R acc = collectionSupplier.get();
                return new Iterator<R>() {
                    
                    boolean has;
                    
                    boolean once;
                    
                    R result = acc;
                    
                    @Override
                    public boolean hasNext() {
                        if (!once) {
                            once = true;
                            
                            R r = result;
                            
                            Iterator<T> sit = it;
                            
                            while (sit.hasNext()) {
                                
                                r = function.apply(r, sit.next());
                            }
                            
                            result = r;
                            has = true;
                        }
                        return has;
                    }
                    @Override
                    public R next() {
                        if (hasNext()) {
                            has = false;
                            return result;
                        }
                        throw new NoSuchElementException();
                    }
                };
            }
        };
    }
    
    static FluentIterable<Integer> range(int start, int count) {
        return new FluentIterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    int index = start;
                    int end = start + count;
                    @Override
                    public boolean hasNext() {
                        return index != end;
                    }
                    @Override
                    public Integer next() {
                        int i = index;
                        if (i != end) {
                            index = i + 1;
                            return i;
                        }
                        throw new IndexOutOfBoundsException("" + i);
                    }
                };
            }
        };
    }
}
