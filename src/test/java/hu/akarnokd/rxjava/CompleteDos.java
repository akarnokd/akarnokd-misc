package hu.akarnokd.rxjava;

import org.junit.Test;
import rx.*;
import rx.schedulers.Schedulers;

public class CompleteDos {

    @Test
    public void test() throws Exception {
        Completable.fromAction(() -> System.out.println("A1"))
        .doOnSubscribe(s -> 
            System.out.println("---> A1 doOnSubscribe")
        )
        .doOnCompleted(() -> System.out.println("---> A1 doOnCompleted"))
        .doOnUnsubscribe(() ->
            System.out.println("---> A1 doOnUnsubscribe")
        )
        .andThen(
                Completable.fromAction(() -> System.out.println("B1")))
        .doOnSubscribe(s ->
            System.out.println("---> B1 doOnSubscribe")
        )
        .doOnCompleted(() -> System.out.println("---> B1 doOnCompleted"))
        .doOnUnsubscribe(() ->
            System.out.println("---> B1 doOnUnsubscribe")
        )
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        .subscribe();
        
        Thread.sleep(10000);
    }
}
