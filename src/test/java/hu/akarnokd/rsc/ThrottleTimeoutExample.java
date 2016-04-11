package hu.akarnokd.rsc;

import java.util.concurrent.*;

import org.junit.Test;

import reactivestreams.commons.publisher.Px;

public class ThrottleTimeoutExample {
    @Test
    public void throttleTimeout() throws Exception {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        
        try {
            Px.fromArray(0, 50, 90, 120)
            .concatMap(v -> Px.timer(v, TimeUnit.MILLISECONDS, exec))
            .throttleTimeout(v -> Px.timer(100, TimeUnit.MILLISECONDS, exec))
            .doOnNext(System.out::println)
            .subscribe();
            
            Thread.sleep(1000);
        } finally {
            exec.shutdown();
        }
    }
}
