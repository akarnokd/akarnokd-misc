package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.*;

public class ObserveOnTest {
    @Test
    public void completeExactRequest() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);

        TestScheduler test = Schedulers.test();

        Observable.range(1, 10).observeOn(test).subscribe(ts);

        test.advanceTimeBy(1, TimeUnit.SECONDS);

        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotCompleted();

        ts.requestMore(10);

        test.advanceTimeBy(1, TimeUnit.SECONDS);

        ts.assertValueCount(10);
        ts.assertNoErrors();
        ts.assertCompleted();
    }
}
