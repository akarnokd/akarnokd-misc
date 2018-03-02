package hu.akarnokd.rxjava;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import rx.Completable;
import rx.schedulers.Schedulers;

public class InterruptFlow {
    @Test
    public void testInterrupt() throws InterruptedException {
       final int count = 1000;
       CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            Completable.complete()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()) // The problem does not occur if you comment out this line
                .andThen(Completable.fromAction(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted!"); // This is output periodically
                    }
                }))
                .subscribe(() -> latch.countDown());
        }

        latch.await();
        //testInterrupt();
    }
}
