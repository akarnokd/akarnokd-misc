package hu.akarnokd.rxjava3;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class TimerDrift {

    public static void main(String[] args) throws Throwable {

        System.setProperty("rx3.scheduler.use-nanotime", "true");

        Schedulers.io().schedulePeriodicallyDirect(() -> {
            System.out.println(LocalDateTime.now());
        }, 1, 1, TimeUnit.SECONDS);

        Thread.sleep(100000);
    }
}
