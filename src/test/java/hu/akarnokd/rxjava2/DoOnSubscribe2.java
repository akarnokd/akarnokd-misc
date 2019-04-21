package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class DoOnSubscribe2 {
    static void printThread(String format, Object... objects) {
        System.out.println(String.format("%s %s", Thread.currentThread().getName(),
                String.format(format, objects)));
    }

    @Test
    public void testFoo() throws InterruptedException {
        Observable.fromCallable(() -> { printThread("callable"); return 1L;})
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(s -> printThread("A"))
                  .doOnSubscribe(s -> printThread("B"))
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(s -> printThread("C"))
                  .subscribeOn(Schedulers.newThread())
                  .doOnSubscribe(s -> printThread("D"))
                  .blockingSubscribe();

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
                   .blockingAwait();
    }
}
