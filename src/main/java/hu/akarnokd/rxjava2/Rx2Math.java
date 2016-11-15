package hu.akarnokd.rxjava2;

import reactor.core.publisher.*;

public enum Rx2Math {
    ;

    public static Mono<Integer> sumInt(Flux<Integer> source) {
        return new SumIntMono(source);
    }

    public static Mono<Long> sumLong(Flux<Long> source) {
        return new SumLongMono(source);
    }

    public static Mono<Integer> maxInt(Flux<Integer> source) {
        return new MaxIntMono(source);
    }

}