package hu.akarnokd.reactor;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import reactor.core.publisher.*;
import reactor.core.tuple.Tuple;

public class TestReactor {

    @Test
    public void test() throws InterruptedException {
        Iterable<Long> iterable = new Iterable<Long>() {
            final AtomicLong count = new AtomicLong(0);
            @Override
            public Iterator<Long> iterator() {
                return new Iterator<Long>() {
                    { System.out.println("startup"); }
                    @Override
                    public boolean hasNext() { return true; }

                    @Override
                    public Long next() {
                        System.out.println("nxt");
                        try { Thread.sleep(200); } catch (InterruptedException e) {}
                        final long l = count.incrementAndGet();
                        System.out.println("inc: "+l);
                        return l;
                    }
                };
            }
        };


        final ConnectableFlux<Long> flux = Flux.fromIterable(iterable).publish();
//        final Flux<Long> flux = Flux.fromIterable(iterable);

        final Flux<Long> intervalOdd =
                flux.filter(p -> {
                    System.out.println("odd "+p);
                    return p % 2 != 0;
                });

        final Flux<Long> intervalEven =
                flux.filter(p -> {
                    System.out.println("even "+p);
                    return p % 2 == 0;
                });

        Flux.combineLatest(intervalOdd, intervalEven, (a,b) -> Tuple.of(a,b))
                .consume(tuple -> System.out.printf("(%d,%d)\n", tuple.getT1(), tuple.getT2()));


        flux.connect();

        Thread.currentThread().join();
    }
}