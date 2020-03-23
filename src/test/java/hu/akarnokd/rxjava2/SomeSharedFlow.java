package hu.akarnokd.rxjava2;

import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class SomeSharedFlow {

    @Test
    public void test() throws Exception {
        Scheduler s1 = Schedulers.from(Executors.newSingleThreadExecutor(r -> new Thread(r, "scheduler1")));
        Scheduler s2 = Schedulers.from(Executors.newSingleThreadExecutor(r -> new Thread(r, "scheduler2")));
        
        Flowable<Long> flowable = Flowable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
                .take(10_000)
                .observeOn(s1)
                .map(v -> {
                    String name = Thread.currentThread().getName();
                    if (!name.equals("scheduler1")) {
                        throw new AssertionError(name);
                    }
                    return v;
                })
                .hide()
                .share();
        
        flowable.observeOn(s2)
            .doOnNext(v -> Thread.sleep(10))
            .subscribe(v -> System.out.println(v + " " + Thread.currentThread().getName()), Throwable::printStackTrace);
        
        Thread.sleep(10000);
    }
}
