package hu.akarnokd.rxjava2;

import java.util.concurrent.*;

import io.reactivex.*;
import io.reactivex.internal.schedulers.SingleScheduler;

public class FirstAndSample {

    public static void main(String[] args) throws Exception {
        int[] delay = { 0, 100, 250, 300, 900, 975, 1050, 1200 };

        Flowable.range(0, delay.length)
        .flatMap(v -> Flowable.timer(delay[v], TimeUnit.MILLISECONDS).map(u -> (v + 1) + " @ " + delay[v] + " ms"))
        .compose(firstAndSample(200, TimeUnit.MILLISECONDS))
        .blockingSubscribe(System.out::println)
        ;
    }
    
    public static <T> FlowableTransformer<T, T> firstAndSample(long delay, TimeUnit unit) {
        return f -> {
            Scheduler s = new SingleScheduler();
            return f.publish(g -> {
               return g.take(1)
               .concatWith(
                   g
                   .sample(delay, unit, s, true)
                   .timeout(delay + 1, unit, s)
               )
               .retry(e -> e instanceof TimeoutException)
               ;
            });
        };
    }
}
