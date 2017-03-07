package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ReplayTimeout {

    @Test
    public void test1x() throws Exception {
        rx.schedulers.TestScheduler scheduler = new rx.schedulers.TestScheduler();
        
        rx.Observable<Integer> source = rx.Observable.just(1)
                .replay(2, TimeUnit.SECONDS, scheduler)
                .autoConnect();

        source.test().assertResult(1);

        source.test().assertResult(1);

        scheduler.advanceTimeBy(3, TimeUnit.SECONDS);

        source.test().assertResult();
    }

    @Test
    public void test2x() throws Exception {
        io.reactivex.schedulers.TestScheduler scheduler = new io.reactivex.schedulers.TestScheduler();
        
        io.reactivex.Observable<Integer> source = io.reactivex.Observable.just(3)
                .replay(2, TimeUnit.SECONDS, scheduler)
                .autoConnect();

        source.test().assertResult(3);

        source.test().assertResult(3);

        scheduler.advanceTimeBy(3, TimeUnit.SECONDS);

        source.test().assertResult();
    }

    @Test
    public void test2xf() throws Exception {
        io.reactivex.schedulers.TestScheduler scheduler = new io.reactivex.schedulers.TestScheduler();
        
        io.reactivex.Flowable<Integer> source = io.reactivex.Flowable.just(3)
                .replay(2, TimeUnit.SECONDS, scheduler)
                .autoConnect();

        source.test().assertResult(3);

        source.test().assertResult(3);

        scheduler.advanceTimeBy(3, TimeUnit.SECONDS);

        source.test().assertResult();
    }
}
