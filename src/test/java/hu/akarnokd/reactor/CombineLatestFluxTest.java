package hu.akarnokd.reactor;

import java.util.Iterator;

import reactor.core.publisher.*;
import reactor.core.tuple.Tuple;

public class CombineLatestFluxTest {
    static class Iter implements Iterator<Long> {
        long count = 0;
        @Override public boolean hasNext() { return true; }
        @Override public Long next() { return count++; }
    };
    public static void main(String[] args) throws Exception {


            final Flux<Long> s1 = Flux.fromIterable(Iter::new).publishOn(SchedulerGroup.single("A"));
            final Flux<Long> s2 = Flux.fromIterable(Iter::new).publishOn(SchedulerGroup.single("B"));


            Runnable r = Flux.combineLatest(
                    v-> Tuple.of((long)v[0],(long)v[1]), 1,
                    s1, s2 //, (l,r) -> Tuple.of(l,r)
            ).consume(t -> System.out.printf("(%d,%d)\n", t.getT1(),t.getT2()));
            
            Thread.sleep(2000);
            r.run();
    }
}
