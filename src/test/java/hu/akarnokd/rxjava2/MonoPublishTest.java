package hu.akarnokd.rxjava2;

import io.reactivex.Flowable;
import reactor.core.publisher.*;

public class MonoPublishTest {

    public static void main(String[] args) {
        /*
        Mono<Integer> simpleMono = Mono.just(1);
        Mono<Integer> publishMulticastMono = simpleMono.publish(m -> m);
        Flux<Mono<Integer>> fluxThatHasPublishMulticastMonos = Flux.just(publishMulticastMono);

        publishMulticastMono.block();
        
        fluxThatHasPublishMulticastMonos
                .flatMap(mono -> mono)  // flatten
                .blockLast();           // fire. Note: No problem with `blockFirst()`
        */
        Flowable<Integer> source = Flowable.just(1);
        Flowable<Flowable<Integer>> s2 = Flowable.just(source.publish(v -> v));
        
        s2.flatMap(v -> v).blockingLast();
    }
}
