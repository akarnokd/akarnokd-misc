package hu.akarnokd.reactor;

import java.time.Duration;
import java.util.concurrent.*;

import org.junit.Test;

import reactor.core.publisher.*;

public class BufferWhenTest {
    static class Wrapper {
        final int i;

        Wrapper(int i) {this.i = i;}

        @Override
        public String toString() {
            return "Wrapper{" +
                    "i=" + i +
                    '}';
        }
    }

    @Test
    public void test() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Sinks.Many<Wrapper> processor = Sinks.many().unicast().onBackpressureBuffer();

        processor.asFlux().buffer(Duration.ofMillis(3000), Duration.ofMillis(2000))
                 .doOnNext(t -> System.out.println(String.format("tuple %s", t)))
                 .subscribe();

        Flux.range(1, Integer.MAX_VALUE)
            .delayElements(Duration.ofMillis(10))
            .doOnNext(i -> processor.tryEmitNext(new Wrapper(i)))
            .doOnComplete(processor::tryEmitComplete)
            .subscribe();

        latch.await(10, TimeUnit.MINUTES);
    }
}
