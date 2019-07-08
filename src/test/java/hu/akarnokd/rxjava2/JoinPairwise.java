package hu.akarnokd.rxjava2;

import java.util.Collections;

import org.junit.Test;

import hu.akarnokd.rxjava2.operators.Flowables;
import io.reactivex.Flowable;

public class JoinPairwise {

    static abstract class Pair implements Comparable<Pair> { 
        int value;

        @Override
        public int compareTo(Pair o) {
            return Integer.compare(value, o.value);
        }
    }
    
    static final class Left extends Pair {
        Left(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "[" + value + ", _]";
        }
    }
    
    static final class Right extends Pair {
        Right(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "[_, " + value + "]";
        }
    }
    
    static final class Both extends Pair {
        Both(int value) {
            this.value = value;
        }
        
        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Both) {
                return ((Both)obj).value == value;
            }
            return false;
        }

        @Override
        public String toString() {
            return "[" + value + ", " + value + "]";
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        Flowable<Integer> a = Flowable.just(0, 2, 3, 6);
        Flowable<Integer> b = Flowable.just(1, 2, 3, 4, 5, 6);

        Flowable.defer(() -> {
            boolean[] skip = { false };
            return Flowables.<Pair>orderedMerge(
                    a.<Pair>map(Left::new), b.<Pair>map(Right::new)
                )
                .distinctUntilChanged()
                .buffer(2, 1)
                .flatMapIterable(buf -> {
                    if (skip[0]) {
                        skip[0] = false;
                        return Collections.emptyList();
                    }
                    if (buf.size() == 2) {
                        if (buf.get(0).value == buf.get(1).value) {
                            skip[0] = true;
                            return Collections.singletonList(new Both(buf.get(0).value));
                        }
                        return buf.subList(0, 1);
                    }
                    return buf;
                });
        })
        .subscribe(System.out::println);
    }
}
