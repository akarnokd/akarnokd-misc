package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

public final class ShareLatest {

    private ShareLatest() {

    }

    public static void main(String[] args) {
        System.out.println(Observable.interval(1, 1, TimeUnit.MILLISECONDS)
        .take(2000)
        .share()
        .onBackpressureLatest()
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation(), 16)
        .doOnNext(System.out::println)
        .toList()
        .toBlocking()
        .last().size());
    }
}
