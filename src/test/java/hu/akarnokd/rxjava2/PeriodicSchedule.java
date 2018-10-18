package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PeriodicSchedule {

    @Test
    public void test() throws Exception {
        final Disposable schedulePeriodicallyDirect = Schedulers.io().schedulePeriodicallyDirect(new Runnable() {
            private int val = 0;

            @Override
            public void run() {
                System.out.println("executed " + (val++));
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        Thread.sleep(2500);
        schedulePeriodicallyDirect.dispose();
        System.out.println("Disposed");

        Thread.sleep(2500);
    }
}
