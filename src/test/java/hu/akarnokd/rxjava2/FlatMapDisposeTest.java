package hu.akarnokd.rxjava2;

import static org.junit.Assert.*;

import org.junit.Test;
import org.reactivestreams.tck.flow.support.TestException;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

public class FlatMapDisposeTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        PublishSubject<Integer> ps3 = PublishSubject.create();

        TestObserver<Integer> to = Observable.fromArray(ps1, ps2, ps3)
        .flatMap(v -> v)
        .test();

        assertTrue(ps1.hasObservers());
        assertTrue(ps2.hasObservers());
        assertTrue(ps3.hasObservers());

        ps2.onError(new TestException());

        assertFalse(ps1.hasObservers());
        assertFalse(ps2.hasObservers());
        assertFalse(ps3.hasObservers());

        to.assertFailure(TestException.class);
    }
}
