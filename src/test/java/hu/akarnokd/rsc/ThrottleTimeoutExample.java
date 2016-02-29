package hu.akarnokd.rsc;

import java.util.concurrent.*;

import org.junit.Test;

import reactivestreams.commons.publisher.PublisherBase;

public class ThrottleTimeoutExample {
    @Test
    public void throttleTimeout() throws Exception {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        
        try {
            PublisherBase.fromArray(0, 50, 90, 120)
            .concatMap(v -> PublisherBase.timer(v, TimeUnit.MILLISECONDS, exec))
            .throttleTimeout(v -> PublisherBase.timer(100, TimeUnit.MILLISECONDS, exec))
            .doOnNext(System.out::println)
            .subscribe();
            
            Thread.sleep(1000);
        } finally {
            exec.shutdown();
        }
    }
}
