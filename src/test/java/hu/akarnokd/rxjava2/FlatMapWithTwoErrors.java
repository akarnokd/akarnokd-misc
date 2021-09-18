package hu.akarnokd.rxjava2;

import static org.junit.Assert.*;

import org.junit.Test;

import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;

public class FlatMapWithTwoErrors {

    @Test
    public void innerCancelled() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();

        pp1
        .flatMap(v -> pp2)
        .test();

        pp1.onNext(1);
        assertTrue("No subscribers?", pp2.hasSubscribers());

        pp1.onError(new Exception());

        assertFalse("Has subscribers?", pp2.hasSubscribers());
    }

    @Test
    public void innerCancelled2() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();

        pp1
        .concatMap(v -> pp2)
        .test();

        pp1.onNext(1);
        assertTrue("No subscribers?", pp2.hasSubscribers());

        pp1.onError(new Exception());

        assertFalse("Has subscribers?", pp2.hasSubscribers());
    }


    @Test
    public void innerCancelled3() {
        PublishSubject<Integer> pp1 = PublishSubject.create();
        PublishSubject<Integer> pp2 = PublishSubject.create();

        pp1
        .flatMap(v -> pp2)
        .test();

        pp1.onNext(1);
        assertTrue("No subscribers?", pp2.hasObservers());

        pp1.onError(new Exception());

        assertFalse("Has subscribers?", pp2.hasObservers());
    }

    @Test
    public void innerCancelled4() {
        PublishSubject<Integer> pp1 = PublishSubject.create();
        PublishSubject<Integer> pp2 = PublishSubject.create();

        pp1
        .concatMap(v -> pp2)
        .test();

        pp1.onNext(1);
        assertTrue("No subscribers?", pp2.hasObservers());

        pp1.onError(new Exception());

        assertFalse("Has subscribers?", pp2.hasObservers());
    }
}
