package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import rx.Observable;

public class ThrottleFirstExample {
    public static void main(String[] args) {
        Observable.just(100, 2000, 3000, 6000, 7000, 10000)
        .flatMap(time -> Observable.timer(time, TimeUnit.MILLISECONDS).map(v -> time))
        .throttleFirst(4000, TimeUnit.MILLISECONDS)
        .toBlocking()
        .forEach(System.out::println);
    }
}
