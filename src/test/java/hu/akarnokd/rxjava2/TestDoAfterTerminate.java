package hu.akarnokd.rxjava2;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;

import io.reactivex.*;
import io.reactivex.functions.Action;

public class TestDoAfterTerminate {

    @Test
    public void test() {
        AtomicBoolean called = new AtomicBoolean(false);
        final Action doAfterTerminate = () -> {
            System.out.println("Call");
            called.set(true);
        };

        AtomicBoolean called2 = new AtomicBoolean(false);
        final Action doAfterTerminate2 = () -> {
            System.out.println("Call2");
            called2.set(true);
        };

        Single.just(123)
                .flatMapPublisher(i1 -> Flowable.just(345, 456)
                        .takeUntil(i2 -> i2 != 345))
                .doOnCancel(() -> {
                    System.out.println("Cancelled0");
                })
                .filter(i -> i != 345)
                .doOnSubscribe(disposable -> {})
                .doAfterTerminate(doAfterTerminate)
                .doOnCancel(() -> System.out.println("Cancelled"))
                .firstOrError()
                .doAfterTerminate(doAfterTerminate2)
                .test()
                .awaitTerminalEvent();

        Assert.assertTrue(called2.get());
        Assert.assertTrue(called.get());
    }
}