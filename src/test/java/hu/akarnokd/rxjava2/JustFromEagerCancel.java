package hu.akarnokd.rxjava2;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;

import io.reactivex.Observable;

public class JustFromEagerCancel {

    @Test
    public void test() {
        AtomicInteger cnt = new AtomicInteger();

        Callable<Integer> a = () -> cnt.incrementAndGet();
        Observable.fromCallable(() -> a)
        .map(v -> v.call())
        .test(true)
        .assertEmpty();

        Assert.assertEquals(0, cnt.get());
    }
}
