package hu.akarnokd.reactor;

import java.util.*;

import org.junit.Test;

import hu.akarnokd.rxjava2.Observable;
import reactor.core.publisher.*;
import reactor.core.test.TestSubscriber;

public class StockTest {
    @Test
    public void stockTest() throws Exception {
        List<Integer> list = Observable.range(1, 100).toList().toBlocking().first();
        TestSubscriber<Object> ts = new TestSubscriber<>();
        
        Flux.interval(3_000).flatMap(v -> 
                Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E"))
                .flatMap(w -> Mono.fromCallable(() -> list).publishOn(SchedulerGroup.async()).flatMap(Flux::fromIterable)))
        .subscribe(ts);
        
        Thread.sleep(50000);
        
        ts.cancel();
        
        ts.assertNoError();
        ts.assertNotComplete();
    }
}
