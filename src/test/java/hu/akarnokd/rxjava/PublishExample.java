package hu.akarnokd.rxjava;

import rx.Observable;

public class PublishExample {

    public static void main(String[] args) {
        Observable.range(1, 10)
        .doOnSubscribe(() -> System.out.println("Subscribed"))
        .publish(o -> Observable.zip(o.map(v -> v * 10), o.map(v -> v * 20), (a, b) -> a + "-" + b))
        .subscribe(System.out::println, Throwable::printStackTrace);

    }
}
