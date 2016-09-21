package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.*;

public class ScanReplay {
    @Test
    public void testExpectedReplayBehavior() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Integer> subject = TestSubject.create(scheduler);
        final TestSubscriber<Integer> subscriber = new TestSubscriber<>();

        final ConnectableObservable<Integer> sums = subject.scan((a, b) -> a + b).replay(1);
        sums.connect();

        subject.onNext(1);
        subject.onNext(2);
        subject.onNext(3);
        scheduler.triggerActions();

        sums.subscribe(subscriber);

        subscriber.assertValueCount(1);
        subscriber.assertValues(6);
    }
    
    @Test
    public void testFlakyReplayBehavior() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Integer> subject = TestSubject.create(scheduler);
        final TestSubscriber<Integer> subscriber = new TestSubscriber<>();

        final ConnectableObservable<Integer> sums = subject.scan(1, (a, b) -> a + b).replay(1);
        sums.connect();

        subject.onNext(2);
        subject.onNext(3);
        scheduler.triggerActions();

        sums.subscribe(subscriber);

//        subscriber.assertValueCount(1);
        subscriber.assertValues(6);
    }
    
    @Test
    public void testFlakyReplayBehavior2() {
        final PublishSubject<Integer> subject = PublishSubject.create();
        final TestSubscriber<Integer> subscriber = new TestSubscriber<>();

        final ConnectableObservable<Integer> sums = subject.scan(1, (a, b) -> a + b).replay(1);
        sums.connect();

        subject.onNext(2);
        subject.onNext(3);

        sums.subscribe(subscriber);

//        subscriber.assertValueCount(1);
        subscriber.assertValues(6);
    }
}
