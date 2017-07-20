package hu.akarnokd.rxjava2;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ambros on 08/07/2017.
 * email: luca.ambro94@gmail.com
 * github: https://github.com/Ambros94
 */
public class NoWebMain {

    public static class LongCouple {
        Long aLong;
        Long bLong;

        public LongCouple(Long aLong, Long bLong) {
            this.aLong = aLong;
            this.bLong = bLong;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Flowable<Long> flowA = Flowable.interval(100, TimeUnit.MILLISECONDS);
        Flowable<Long> flowB = Flowable.interval(200, TimeUnit.MILLISECONDS);

        Flowable<LongCouple> combined = Flowable.combineLatest(Arrays.asList(flowA.onBackpressureLatest(), flowB.onBackpressureLatest()),
                a -> new LongCouple((Long) a[0], (Long) a[1]),
                1)
                .onBackpressureLatest()
                .observeOn(Schedulers.newThread(), false, 1);
        combined.subscribe((longCouple -> {
            System.out.println(longCouple.aLong + ":" + longCouple.bLong);
            Thread.sleep(1000);
        }));
        Thread.sleep(10000000);
    }
}