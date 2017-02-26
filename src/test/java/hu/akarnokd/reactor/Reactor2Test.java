package hu.akarnokd.reactor;
import java.util.Collections;

import reactor.core.publisher.Flux;

public final class Reactor2Test {
    private Reactor2Test() { }

    public static void main(String... args) {
        Flux<Integer> source = Flux.create((e) -> {
            for (int i = 0; i < 300; i++) {
                e.next(i);
            }
            e.complete();
        });
        Flux<Integer> cached = source.cache();

        long sourceCount = source.concatMapIterable(Collections::singleton)
                .distinct().count().block();
        long cachedCount = cached.concatMapIterable(Collections::singleton)
                .distinct().count().block();

        System.out.println("source: " + sourceCount);
        System.out.println("cached: " + cachedCount);
    }
}