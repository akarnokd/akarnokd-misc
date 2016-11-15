package hu.akarnokd.reactor;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class GroupParallel {

    @Test
    public void parallelGroupComputation() throws Exception {

        AtomicInteger wip = new AtomicInteger();

        Flux.range(1, 1000_000)
        .groupBy(i -> i % 8)
        .flatMap(g -> g)
        .parallel()
        .runOn(Schedulers.parallel())
        .sequential()
        .subscribe(c -> {

            if (wip.getAndIncrement() != 0) {
                System.err.println("Concurrent invocation!");
            }
            try {
                Thread.sleep(1);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            wip.decrementAndGet();
        });

        Thread.sleep(100000);
    }
}
