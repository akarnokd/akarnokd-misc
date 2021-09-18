package hu.akarnokd.rxjava3;

import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.CountDownLatch;

import org.testng.annotations.Test;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SchedulerPrint {

    @Test
    public void disposablePrint() {
        CountDownLatch cdl = new CountDownLatch(1);
        try {
            Disposable d = Schedulers.single().scheduleDirect(() -> {
                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            assertNotEquals("", d.toString());
        } finally {
            cdl.countDown();
        }
    }

}
