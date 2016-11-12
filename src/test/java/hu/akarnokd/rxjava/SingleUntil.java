package hu.akarnokd.rxjava;

import java.util.concurrent.CancellationException;

import org.junit.Test;

import rx.Single;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public class SingleUntil {
    @Test
    public void singleTakeUntil() {
        PublishSubject<String> controller = PublishSubject.create();

        TestSubscriber<String> testSubscriber = new TestSubscriber<>(0);

        Single.just("Hello")
            .toObservable()
            .takeUntil(controller.map(v -> { throw new CancellationException(); }))
            .subscribe(testSubscriber);

        controller.onNext("Stop flow");

        testSubscriber.requestMore(1);
        testSubscriber.assertNoValues();
        testSubscriber.assertError(CancellationException.class);
    }
}
