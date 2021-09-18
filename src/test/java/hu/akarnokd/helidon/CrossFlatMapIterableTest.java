package hu.akarnokd.helidon;

import java.util.Arrays;

import org.junit.Test;

import io.helidon.common.reactive.Multi;

public class CrossFlatMapIterableTest {

    static void cross(int times) {
        Integer[] outerArray = new Integer[times];
        Arrays.fill(outerArray, 777);
        Iterable<Integer> outerIterable = Arrays.asList(outerArray);

        Integer[] innerArray = new Integer[1_000_000 / times];
        Arrays.fill(innerArray, 777);
        Iterable<Integer> innerIterable = Arrays.asList(innerArray);

        Multi.create(outerIterable).flatMapIterable(v -> innerIterable)
        .subscribe(v -> { }, Throwable::printStackTrace);
    }

    @Test
    public void cross1() {
        cross(1);
    }

    @Test
    public void cross10() {
        cross(10);
    }

    @Test
    public void cross100() {
        cross(100);
    }

    @Test
    public void cross1000() {
        cross(1000);
    }

    @Test
    public void cross10000() {
        cross(10000);
    }

    @Test
    public void cross100000() {
        cross(100000);
    }

    @Test
    public void cross1000000() {
        cross(1000000);
    }
}
