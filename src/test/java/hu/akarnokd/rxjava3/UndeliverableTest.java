package hu.akarnokd.rxjava3;

import java.io.IOException;

import org.junit.Test;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class UndeliverableTest {

    @Test
    public void test() {
        RxJavaPlugins.setErrorHandler(error -> System.out.println(error));

        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> inner = PublishProcessor.create();

        // switchMapDelayError will delay all errors
        TestSubscriber<Integer> ts = main.switchMapDelayError(v -> inner).test();

        main.onNext(1);

        // the inner fails
        inner.onError(new IOException());

        // the consumer is still clueless
        ts.assertEmpty();

        // the consumer cancels
        ts.cancel();

    }
}
