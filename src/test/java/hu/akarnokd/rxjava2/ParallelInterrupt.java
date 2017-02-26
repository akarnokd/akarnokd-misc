package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public final class ParallelInterrupt {

    private ParallelInterrupt() { }

    public static void main(String[] args) {
        RxJavaPlugins.setErrorHandler(e -> { });
        Flowable.range(1, 10)
        .parallel(4)
        .runOn(Schedulers.io())
        .map(v -> {
           if (v == 2) {
              throw new IOException();
           }
           Thread.sleep(2000);
           return v;
        })
        .sequential()
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertFailure(IOException.class);
    }
}
