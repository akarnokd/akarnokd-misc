package hu.akarnokd.rxjava;

import java.io.IOException;

import org.junit.Test;

import rx.*;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class FromToSingle {
    @Test
    public void test() throws Exception {
        Observable.fromCallable(() -> { throw new IOException(); })
        .toSingle()
        .subscribeOn(Schedulers.computation())
        .toObservable()
        .toSingle()
        .onErrorResumeNext(v -> Single.just(1))
        .subscribe(System.out::println, Throwable::printStackTrace);

        Thread.sleep(1000);
    }

    @Test
    public void plainSubscribeCheck() {
        Single.<String>fromCallable(() -> {
            throw new IllegalStateException();
        })
        .subscribeOn(Schedulers.immediate())
        .toObservable()
        .onErrorResumeNext(throwable -> Observable.empty())
        .subscribe(new TestSubscriber<>());

        Observable.<String>fromCallable(() -> {
            throw new IllegalStateException();
        })
        .subscribeOn(Schedulers.immediate())
        .onErrorResumeNext(throwable -> Observable.empty())
        .subscribe(new TestSubscriber<>());
    }
}
