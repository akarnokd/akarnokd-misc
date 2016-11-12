package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.Single;
import rx.schedulers.Schedulers;

public class SingleUnsubscribe {
    @Test
    public void test() throws Exception {
        Single.fromCallable(() -> 42)
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        .doOnSubscribe(() -> System.out.println("subscribed"))
        .doOnUnsubscribe(() -> System.out.println("unsubscribed"))
        .subscribe(integer -> System.out.println("got result"));

        Thread.sleep(1000);
    }
}
