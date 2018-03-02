package hu.akarnokd.rxjava2;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class MergeAmbTest {

    @Test
    public void testMergeOperator() {
        TestObserver<String> observer = new TestObserver<>();

        Observable<String> foo = Observable.just("FOO");
        Observable<String> bar = Observable.timer(2, TimeUnit.SECONDS).map(v -> "BAR");

        foo.publish(first -> Observable.merge(first, bar.takeUntil(first))
                .firstOrError()
                .toObservable())
                .subscribe(observer);

        assertEquals(1, observer.valueCount());
        assertEquals("FOO", observer.values().get(0));
    }
}
