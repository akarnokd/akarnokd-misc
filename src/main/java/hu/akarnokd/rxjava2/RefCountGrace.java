package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.schedulers.TestScheduler;

public class RefCountGrace {

    public static void main(String[] args) {
        TestScheduler sch = new TestScheduler();

        Flowable<Integer> f = Flowable.just(1).replay(1)
                .refCount(1, 10, TimeUnit.MILLISECONDS, sch);

        f.subscribe();

        sch.advanceTimeBy(10, TimeUnit.MILLISECONDS);

        f.test().assertResult(1);

        sch.advanceTimeBy(10, TimeUnit.MILLISECONDS);

        f.test().assertResult(1);
    }
}
