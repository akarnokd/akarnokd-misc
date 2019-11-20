package hu.akarnokd.reactor;

import java.util.*;

import org.junit.Test;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class DoOnSubscribeTest {

    @Test
    public void test() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 100000; i++) {
            Mono<Integer> source = Mono.just(1).doOnSubscribe(s -> set.add(Thread.currentThread().getName()));

            Mono.just(1)
            .publishOn(Schedulers.single())
            .flatMap(v -> source)
            .ignoreElement()
            .block();
        }

        System.out.println(set);
    }
}
