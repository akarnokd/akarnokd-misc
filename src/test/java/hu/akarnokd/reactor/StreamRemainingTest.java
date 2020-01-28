package hu.akarnokd.reactor;

import java.util.stream.*;

import org.junit.Test;

public class StreamRemainingTest {

    @Test
    public void test() {
        Stream<Integer> source = IntStream.range(1, 11).boxed();

        Stream<Integer> result = source.onClose(() -> {
            source.iterator().forEachRemaining(System.out::println);
        });

        try (Stream<Integer> stream = result.limit(5)) {
            stream.forEach(v -> System.out.println("Consumed: " + v));
        }
    }
}
