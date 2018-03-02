package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Flowable;

public class BufferExactDisposeTest {

    @Test
    public void test() {
        Flowable.range(1, 5)
        .doOnCancel(() -> new Exception().printStackTrace())
        .takeUntil(v -> v == 4)
        .buffer(5, TimeUnit.SECONDS)
        .blockingLast();
    }
}
