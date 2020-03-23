package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.*;

import io.reactivex.*;
import io.reactivex.observers.TestObserver;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subscribers.TestSubscriber;

public class ReplayRefCountSubjectTest {

@Test
public void test() {
    BehaviorSubject<Integer> subject = BehaviorSubject.create();

    Observable<Integer> observable = subject
            .doOnNext(e -> { 
                System.out.println("This emits for second subscriber"); 
            })
            .doOnSubscribe(s -> System.out.println("OnSubscribe"))
            .doOnDispose(() -> System.out.println("OnDispose"))
            .replay(1)
            .refCount()
            .doOnNext(e -> { System.out.println("This does NOT emit for second subscriber"); });

    System.out.println("Subscribe-1");
    // This line causes the test to fail.
    observable.takeUntil(Observable.just(1)).test();

    subject.onNext(2);

    System.out.println("Subscribe-2");
    TestObserver<Integer> subscriber = observable.take(1).test();
    Assert.assertTrue(subscriber.awaitTerminalEvent(2, TimeUnit.SECONDS));
}


@Test
public void test2() {
    BehaviorProcessor<Integer> subject = BehaviorProcessor.create();

    Flowable<Integer> observable = subject
            .doOnNext(e -> { 
                System.out.println("This emits for second subscriber"); 
            })
            .doOnSubscribe(s -> System.out.println("OnSubscribe"))
            .doOnCancel(() -> System.out.println("OnDispose"))
            .replay(1)
            .refCount()
            .doOnNext(e -> { System.out.println("This does NOT emit for second subscriber"); });

    System.out.println("Subscribe-1");
    // This line causes the test to fail.
    observable.takeUntil(Flowable.just(1)).test();

    subject.onNext(2);

    System.out.println("Subscribe-2");
    TestSubscriber<Integer> subscriber = observable.take(1).test();
    Assert.assertTrue(subscriber.awaitTerminalEvent(2, TimeUnit.SECONDS));
}
}
