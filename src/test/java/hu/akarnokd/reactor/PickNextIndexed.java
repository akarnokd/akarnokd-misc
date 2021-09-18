package hu.akarnokd.reactor;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.reactivex.internal.queue.SpscLinkedArrayQueue;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class PickNextIndexed {

    @Test
    public void test() {
        int parallelism = 8;
        int bufferSize = 256;
        AtomicInteger indexer = new AtomicInteger();

        @SuppressWarnings("unchecked")
        SpscLinkedArrayQueue<AtomicInteger>[] queues = new SpscLinkedArrayQueue[parallelism];
        for (int i = 0; i < queues.length; i++) {
            queues[i] = new SpscLinkedArrayQueue<AtomicInteger>(bufferSize);
        }

        Flux.range(0, 1000)
        .map(AtomicInteger::new)
        .parallel(parallelism)
        .runOn(Schedulers.parallel())
        .map(v -> {
            int rail = (int)Thread.currentThread().getId() % parallelism;
            System.out.println("> " + v.get() + " -> " + rail);
            queues[rail].offer(v);
            return 1;
        })
        .sequential()
        .map(ignore -> {
            for (SpscLinkedArrayQueue<AtomicInteger> q : queues) {
                AtomicInteger v = q.peek();
                if (v != null && v.get() == indexer.get()) {
                    q.poll();
                    indexer.lazySet(v.get() + 1);
                    return v;
                }
            }
            throw new IllegalStateException("Queue inconsistency! " + indexer.get());
        })
        .toStream()
        .forEach(System.out::println);
    }


}
