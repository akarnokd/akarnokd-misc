package hu.akarnokd.rxjava2;

import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class SingleTimeout7515Rx3 {

    @Test
    public void test() throws Exception {
        for (int i = 0; i < 10000000; i++) {
            final int y = i;
            final CountDownLatch latch = new CountDownLatch(1);
            Disposable d = Single.never()
                    .timeout(0, TimeUnit.NANOSECONDS, Schedulers.computation())
                    .subscribe(v -> {}, e -> {
                        System.out.println("timeout " + y);
                        latch.countDown();
                    });
            if (!latch.await(2, TimeUnit.SECONDS)) {
                System.out.println(d);
                throw new IllegalStateException("Timeout was not happening!");
            }
        }
    }
}
