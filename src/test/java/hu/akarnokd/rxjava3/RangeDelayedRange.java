package hu.akarnokd.rxjava3;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RangeDelayedRange {
    public static void main(String[] args) {
        Flowable.range(0, 20)
                .doOnNext(run -> System.out.println("run: " + run))
                .concatMap(run -> Flowable.range(0, 1000)
                        .flatMap(RangeDelayedRange::stubFunction)
                        .doOnNext(iteration -> System.out.println("iteration: " + iteration))
                        .subscribeOn(Schedulers.io())
                )
                .count()
                .blockingSubscribe(v -> System.out.printf("%n%n---------%n%s", v));
    }

    private static Flowable<Integer> stubFunction(int iteration) {
        return Flowable.just(iteration)
                .delay(100, TimeUnit.MILLISECONDS);
    }
}
