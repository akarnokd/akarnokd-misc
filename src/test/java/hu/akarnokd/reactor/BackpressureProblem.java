package hu.akarnokd.reactor;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import io.reactivex.subscribers.TestSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class BackpressureProblem {
    @Test
    public void fluxCreateDemoElasticScheduler2() throws Exception {
        final int inputCount = 1000;
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Flux.<Integer>create(
                sink -> {
                    for (int i = 0; i < inputCount; i++) {
                        //logger.info("Injecting {}", i);
                        sink.next(i);
                    }
                    sink.complete();
                }).
                onBackpressureError().subscribe(ts);
        
        ts.assertNoValues()
        .assertError(IllegalStateException.class)
        .assertNotComplete();
    }
    
    @Test
    public void fluxCreateDemoElasticScheduler() throws Exception {
        final int inputCount = 1000;
        final CountDownLatch latch = new CountDownLatch(inputCount);
        Flux.<Integer>create(
                sink -> {
                    for (int i = 0; i < inputCount; i++) {
                        //logger.info("Injecting {}", i);
                        sink.next(i);
                    }
                    sink.complete();
                }).
                onBackpressureError().
                subscribeOn(Schedulers.newSingle("production")).
                publishOn(Schedulers.elastic()).
                subscribe(i -> {
                    //logger.info("Consuming {}", i);
                    if (i == 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            
                        }
                    }
                    latch.countDown();
                }, e -> {
                    e.printStackTrace();
                    latch.countDown();
                });
        
        latch.await();
    }
}
