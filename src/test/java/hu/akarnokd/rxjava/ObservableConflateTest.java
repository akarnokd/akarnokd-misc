package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.observers.AssertableSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;

public class ObservableConflateTest {

    @Test
    public void normal() {
        TestScheduler scheduler = new TestScheduler();

        PublishSubject<Integer> source = PublishSubject.create();
        
        AssertableSubscriber<Integer> ts = source.compose(new ObservableConflate<>(1, TimeUnit.SECONDS, scheduler))
        .test();

        source.onNext(1);
        
        ts.assertValue(1);
        
        source.onNext(2);

        ts.assertValue(1);

        source.onNext(3);

        ts.assertValue(1);

        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        ts.assertValues(1, 3);
        
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        ts.assertValues(1, 3);

        source.onNext(4);

        ts.assertValues(1, 3, 4);

        source.onNext(5);
        source.onCompleted();

        ts.assertResult(1, 3, 4, 5);

        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        ts.assertResult(1, 3, 4, 5);
    }
}
