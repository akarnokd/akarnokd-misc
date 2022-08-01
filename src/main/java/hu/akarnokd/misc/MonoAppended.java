package hu.akarnokd.misc;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MonoAppended {
    public static void main(String[] args) {
        
        Mono<List<String>> source = Flux.just("A", "B", "C").collectList();
        
        source
        .doOnNext(list -> list.add(0, "All"))
        .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
    }
}
