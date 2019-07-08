package hu.akarnokd.rxjava2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

public class SortZip {

    @Test
    public void testRx() {
        for (int i = 1; i < 1000; i++) {
            final int j = i;
            Flowable.range(1, i).map(v -> j - v)
            .sorted()
            .zipWith(Flowable.range(1, Integer.MAX_VALUE), (a, b) -> Tuples.of(a, b))
            .toList()
            .map(list -> list.size())
            .test()
            .assertResult(i);
        }
    }

    @Test
    public void testReactor() {
        for (int i = 1; i < 1000; i++) {
            final int j = i;
            int k = Flux.range(1, i).map(v -> j - v)
            .sort()
            .zipWith(Flux.range(1, Integer.MAX_VALUE), (a, b) -> Tuples.of(a, b))
            .collectList()
            .map(list -> list.size())
            .block();
            assertEquals(j, k);
        }
    }
}
