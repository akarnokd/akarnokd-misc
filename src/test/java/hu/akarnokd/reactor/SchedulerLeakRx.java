package hu.akarnokd.reactor;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.reactivestreams.Subscription;

import hu.akarnokd.rxjava2.schedulers.ParallelScheduler;
import io.reactivex.*;
import io.reactivex.internal.schedulers.SingleScheduler;
import reactor.core.publisher.BaseSubscriber;

public class SchedulerLeakRx {
    @Test
    public void test() throws Exception {
        AtomicLong counter = new AtomicLong(0L);
        Scheduler scheduler = new ParallelScheduler(10);
        Flowable
        .<Long>generate(sink -> sink.onNext(counter.getAndIncrement()))
        .concatMap(i -> {
            return Flowable.just(i)
                    .observeOn(scheduler)
                    .map(number -> number * 2L);
        })
        .subscribeOn(new SingleScheduler())
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
