package hu.akarnokd.reactor;

import java.util.Iterator;
import java.util.function.Function;

import reactor.core.publisher.*;

public class ReactorPublish {
    static class Iter implements Iterator<Long> {
        long count = 0;
        @Override public boolean hasNext() { return true; }
        @Override public Long next() { return count++; }
    };
    final Flux<Long> doubleFlux = Flux.fromIterable(Iter::new).as(DropOld());

    void stream() {

        doubleFlux.as(DropOld()).map(this::fastComputation).consume(i -> System.out.println("1) " + i));
        doubleFlux.as(DropOld()).map(this::slowComputation).consume(i -> System.err.println("2) " + i));
    }

    public static <T> Function<Flux<T>, Flux<T>> DropOld() {
        return (flux) -> flux.onBackpressureLatest().publishOn(Computations.single(), 1);
    }

    public static <T> Function<Flux<T>, Flux<T>> Shared() {
        return (flux) -> flux.publishOn(Computations.single()).publish().refCount();
    }


    <T> T fastComputation(T in) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return in;
    }

    <T> T slowComputation(T in) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return in;
    }

    public static void main(String[] args) throws InterruptedException {
        new ReactorPublish().stream();
        Thread.currentThread().join();
    }
}