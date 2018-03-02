package hu.akarnokd.rxjava2;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class InterruptFlow2 {
    @Test
    public void testInterrupt() throws InterruptedException {
       final int count = 1000;
       CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            Completable.complete()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()) // The problem does not occur if you comment out this line
                //.doOnDispose(() -> System.out.println("Dispose on: " + Thread.currentThread()))
                .andThen(Completable.fromAction(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted! " + Thread.currentThread()); // This is output periodically
                    }
                }))
                .subscribe(() -> latch.countDown());
        }

        latch.await();
        //testInterrupt();
    }
}
