package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class TrampolineInfinite {

    public static void main(String[] args) throws Exception {
        System.out.println("start");
        // snippt-1
        Observable.intervalRange(0L, 10L, 1, 1, TimeUnit.SECONDS, Schedulers.trampoline())
                .subscribe(aLong -> System.out.println(aLong + "; " + Thread.currentThread().getName()), Throwable::printStackTrace,
                        () -> System.out.println("complete1."));
        // snippt-2
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.trampoline()).take(10)
                .subscribe(aLong -> System.out.println(aLong + "; " + Thread.currentThread().getName()), Throwable::printStackTrace,
                        () -> System.out.println("complete2."));

        Thread.sleep(30000);
        System.out.println("end");
    }
}
