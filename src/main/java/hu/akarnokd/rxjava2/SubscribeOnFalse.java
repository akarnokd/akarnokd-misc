package hu.akarnokd.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class SubscribeOnFalse {

    public static void main(String[] args) {
        Flowable.range(1, 10)
        .subscribeOn(Schedulers.io(), true)
        .doOnNext(v -> System.out.println(Thread.currentThread().getName()))
        .observeOn(Schedulers.single(), false, 1)
        .blockingSubscribe();
    }
}
