package hu.akarnokd.rxjava;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class CompletableConcat2 {

    @Test
    public void test() throws Exception {
        System.out.println("Testing Completable.concat");
        System.out.println();
        Completable.concat(
                Flowable.range(1, 5).map(integer -> Completable.fromAction(() -> {
                    try {
                        System.out.println("Processing " + integer + "...");
                        Thread.sleep(100);
                        System.out.println("Processing " + integer + " finished");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .doOnDispose(() -> System.out.println("Unsubscribed from " + integer))
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.computation()))
                .doOnCancel(() -> System.out.println("Unsubscribed from parent observable"))
        ).subscribe(() -> {
                    System.out.println("Finished Completable.concat");
                }, Throwable::printStackTrace
        );
        
        Thread.sleep(2000);
    }
}
