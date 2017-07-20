package hu.akarnokd.reactor;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.reactivestreams.Subscription;

import reactor.core.publisher.*;
import reactor.core.scheduler.*;

public class SchedulerLeak {
    @Test
    public void test() throws Exception {
        AtomicLong counter = new AtomicLong(0L);
        Scheduler scheduler = Schedulers.newParallel("scheduler", 10);
        Flux
        .<Long>generate(sink -> sink.next(counter.getAndIncrement()))
        .concatMap(i -> {
            return Mono.just(i)
                    .publishOn(scheduler)
                    .map(number -> number * 2L);
        })
        .subscribeOn(Schedulers.newSingle("subscriber"))
        .subscribe(new BaseSubscriber<Long>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(10);
            }

            @Override
            protected void hookOnNext(Long value) {
                upstream().request(1);
            }
        });

        Thread.sleep(100000000);
    }
}
