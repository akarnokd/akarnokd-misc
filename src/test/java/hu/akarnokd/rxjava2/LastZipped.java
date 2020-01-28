package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subscribers.TestSubscriber;

public class LastZipped {

    @Test
    public void test() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();

        TestSubscriber<Integer> ts = Flowable.zip(pp1.last(1).toFlowable(), pp2.last(2).toFlowable(), (a, b) -> a + b)
        .test();

        pp1.onNext(3);
        pp1.onComplete();
        pp2.onComplete();

        ts.assertResult(5);
    }
}
