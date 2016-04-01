package hu.akarnokd.reactor;

import reactor.core.publisher.Flux;

public class RepeatTest {
    public static void main(String[] args) {
        int[] c = { 0 };
        Flux.just(1)
        .repeatWhen(o -> o.takeWhile(e -> c[0]++ != 2))
        .consume(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

        System.out.println("---");
        
        int[] d = { 0 };
        Flux.just(1)
        .repeat(() -> d[0]++ != 2)
        .consume(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

        System.out.println("---");

        Flux.just(1)
        .repeat(() -> true)
        .take(5)
        .consume(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

        System.out.println("---");
        
        Flux.error(new RuntimeException())
        .retry(e -> false)
        .consume(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

        System.out.println("---");
        Flux.error(new RuntimeException())
        .retryWhen(o -> o.takeWhile(e -> false))
        .consume(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

        System.out.println("---");
        Flux.error(new RuntimeException())
        .retryWhen(v -> v.flatMap(e -> false ? Flux.just(1) : Flux.error(e)))
        .consume(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

    }
}
