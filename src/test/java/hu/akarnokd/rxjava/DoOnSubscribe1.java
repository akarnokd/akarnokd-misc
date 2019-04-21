package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.*;
import rx.schedulers.Schedulers;

public class DoOnSubscribe1 {
    static void printThread(String format, Object... objects) {
        System.out.println(String.format("%s %s", Thread.currentThread().getName(),
                String.format(format, objects)));
    }

    @Test
    public void testFoo() throws InterruptedException {
        Observable.fromCallable(() -> { printThread("callable"); return 1L;})
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(() -> printThread("A"))
                  .doOnSubscribe(() -> printThread("B"))
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(() -> printThread("C"))
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(() -> printThread("D"))
                  .toBlocking()
                  .subscribe();

        printThread("next!");

        Completable.fromCallable(() -> { printThread("callable"); Thread.sleep(10_000); return 1L;})
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(a -> printThread("A"))
                  .doOnSubscribe(a -> printThread("B"))
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(a -> printThread("C"))
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(a -> printThread("D"))
                   .andThen(Completable.fromAction(() -> printThread("E")))
                   .andThen(Completable.fromAction(() -> printThread("F")).subscribeOn(Schedulers.newThread()))
                   .await();
    }
}
