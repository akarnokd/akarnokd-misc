package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.*;
import rx.schedulers.Schedulers;

public class CompletableConcat1 {

    @Test
    public void test() throws Exception {
        System.out.println("Testing Completable.concat");
        System.out.println();
        Completable.concat(
                Observable.range(1, 5).map(integer -> Completable.fromAction(() -> {
                    try {
                        System.out.println("Processing " + integer + "...");
                        Thread.sleep(100);
                        System.out.println("Processing " + integer + " finished");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .doOnUnsubscribe(() -> System.out.println("Unsubscribed from " + integer))
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
                )
        .doOnUnsubscribe(() -> System.out.println("Unsubscribed from parent observable"))
        ).subscribe(() -> {
                    System.out.println("Finished Completable.concat");
                }, Throwable::printStackTrace
        );
        
        Thread.sleep(2000);
    }
}
