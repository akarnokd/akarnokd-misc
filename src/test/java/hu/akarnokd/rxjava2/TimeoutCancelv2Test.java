package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.observers.TestObserver;
import io.reactivex.processors.*;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.*;
import io.reactivex.subscribers.TestSubscriber;

public class TimeoutCancelv2Test {

    @Test
    public void test4() throws Exception {
        TestScheduler sch = new TestScheduler();

        Subject<Long> subject = PublishSubject.create();
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

        TestObserver<Long> subscriber = new TestObserver<>();
        initialObservable
        .doOnDispose(() -> {
            System.out.println("Unsubscribed");
            new Exception().printStackTrace(System.out);
        })
        .timeout(1, TimeUnit.SECONDS, sch, timeoutObservable).subscribe(subscriber);
        subject.onNext(5L);
        sch.advanceTimeBy(2, TimeUnit.SECONDS);
        subject.onNext(10L);
        subject.onComplete();

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(5L, 10L);
    }

    @Test
    public void test5() throws Exception {
        TestScheduler sch = new TestScheduler();

        FlowableProcessor<Long> subject = PublishProcessor.create();
        Flowable<Long> initialObservable = subject.share()
        .map(value -> {
            System.out.println("Received value " + value);
            //new Exception().printStackTrace(System.out);
            return value;
        });

        Flowable<Long> timeoutObservable = initialObservable.map(value -> {
           System.out.println("Timeout received value " + value);
           return value;
        });

        TestSubscriber<Long> subscriber = new TestSubscriber<>();
        initialObservable
        .doOnCancel(() -> {
            System.out.println("Unsubscribed");
            new Exception().printStackTrace(System.out);
        })
        .timeout(1, TimeUnit.SECONDS, sch, timeoutObservable).subscribe(subscriber);
        subject.onNext(5L);
        sch.advanceTimeBy(2, TimeUnit.SECONDS);
        subject.onNext(10L);
        subject.onComplete();

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(5L, 10L);
    }

}
