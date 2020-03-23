package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;

public class WindowInterrupted {

    @Test
    public void test() {
        Observable
        .intervalRange(0, 2, 0, 1, TimeUnit.SECONDS)
        .window(1, TimeUnit.SECONDS)
        .doOnNext(i -> {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() < start+1500) {
            }
            if (Thread.interrupted()) {
                System.out.println("WTF? Thread is interrupted! " + Thread.currentThread());
            }
        })
        .ignoreElements()
        .blockingAwait();
    }
}
