package hu.akarnokd.reactor;

import org.junit.*;

import reactor.core.publisher.*;

public class FilterWhenTest {

@Test
public void filterAllOut() {
    int[] count = { 0, 0 };
    Flux.range(1, 1000)
    .doOnNext(v -> count[0]++)
    .doOnRequest(System.out::println)
    .filterWhen(o1 -> Mono.just(false)) //REMOVE
    .flatMap(s -> Flux.just(new Object()).hide()) //REMOVE
    .subscribe();

    Assert.assertEquals(1000, count[0]);
}
}
