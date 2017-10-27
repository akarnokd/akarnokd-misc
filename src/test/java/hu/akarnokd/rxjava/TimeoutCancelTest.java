package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import org.junit.*;

import rx.Observable;
import rx.observers.*;
import rx.schedulers.TestScheduler;
import rx.subjects.*;

public class TimeoutCancelTest {

    @Test
    public void test() {
PublishSubject<Integer> ps = PublishSubject.create();
TestScheduler sch = new TestScheduler();

AssertableSubscriber<Integer> as = ps.timeout(1, TimeUnit.SECONDS, Observable.just(1), sch)
.test();

sch.advanceTimeBy(1, TimeUnit.SECONDS);

Assert.assertFalse(ps.hasObservers());

as.assertResult(1);
    }
    
    @Test
    public void test2() throws Exception {
        Subject<Long, Long> subject = PublishSubject.create();
        Observable<Long> initialObservable = subject.share()
        .map(value -> {
            System.out.println("Received value " + value);
            new Exception().printStackTrace(System.out);
            return value;
        });

        Observable<Long> timeoutObservable = initialObservable.map(value -> {
           System.out.println("Timeout received value " + value);
           return value;
        });

        TestSubscriber<Long> subscriber = new TestSubscriber<>();
        initialObservable
        .doOnUnsubscribe(() -> System.out.println("Unsubscribed"))
        .timeout(1, TimeUnit.SECONDS, timeoutObservable).subscribe(subscriber);
        subject.onNext(5L);
        Thread.sleep(1500);
        subject.onNext(10L);
        subject.onCompleted();

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(5L, 10L);
    }

    @Test
    public void test3() throws Exception {
        TestScheduler sch = new TestScheduler();

        Subject<Long, Long> subject = PublishSubject.create();
        Observable<Long> initialObservable = subject.share()
        .map(value -> {
            System.out.println("Received value " + value);
            //new Exception().printStackTrace(System.out);
            return value;
        });

        Observable<Long> timeoutObservable = initialObservable.map(value -> {
           System.out.println("Timeout received value " + value);
           return value;
        });

        TestSubscriber<Long> subscriber = new TestSubscriber<>();
        initialObservable
        .doOnUnsubscribe(() -> System.out.println("Unsubscribed"))
        .timeout(1, TimeUnit.SECONDS, timeoutObservable, sch).subscribe(subscriber);
        subject.onNext(5L);
        sch.advanceTimeBy(2, TimeUnit.SECONDS);
        subject.onNext(10L);
        subject.onCompleted();

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(5L, 10L);
    }
}
