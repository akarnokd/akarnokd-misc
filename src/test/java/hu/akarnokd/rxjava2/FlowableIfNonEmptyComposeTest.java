package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Function;

import org.junit.Test;

import io.reactivex.*;
import reactor.core.publisher.*;

public class FlowableIfNonEmptyComposeTest {

    @Test
    public void simple() throws Exception {
        Flux.range(1, 10)
        .hide()
        .compose(composeIfNonEmpty(f -> {
            System.out.println("Composed!");
            return Mono.from(f.doOnNext(System.out::println));
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void error() {
        Flux.error(new IOException())
        .compose(composeIfNonEmpty(f -> {
            System.out.println("Composed!");
            return Mono.from(f.doOnNext(System.out::println));
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void simpleRx() {
        Flowable.range(1, 10)
        .doOnNext(v -> {
            System.out.println(">> " + v);
        })
        .compose(composeIfNonEmptyRx(f -> {
            System.out.println("Composed!");
            return f.doOnNext(System.out::println).ignoreElements().<Void>toFlowable();
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void errorRx() {
        Flowable.error(new IOException())
        .compose(composeIfNonEmptyRx(f -> {
            System.out.println("Composed!");
            return f.doOnNext(System.out::println).ignoreElements().toFlowable();
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    static <T> Function<Flux<T>, Mono<Void>> composeIfNonEmpty(Function<? super Flux<T>, ? extends Mono<Void>> f) {
        return g ->
            g.publish(h -> 
                h.limitRequest(1).concatMap(first -> f.apply(h.startWith(first)))
            ).ignoreElements();
    }

    static <T> FlowableTransformer<T, Void> composeIfNonEmptyRx(Function<? super Flowable<T>, ? extends Flowable<Void>> f) {
        return g ->
            g.publish(h -> 
                h.limit(1).concatMap(first -> f.apply(h.startWith(first)))
            ).ignoreElements().toFlowable();
    }
}
