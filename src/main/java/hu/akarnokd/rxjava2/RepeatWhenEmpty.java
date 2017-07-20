package hu.akarnokd.rxjava2;

import rx.Observable;

public class RepeatWhenEmpty {

    public static void main(String[] args) {
        Observable.just(1)
        .repeatWhen(o -> Observable.empty())
        .subscribe(System.out::println);
    }
}
