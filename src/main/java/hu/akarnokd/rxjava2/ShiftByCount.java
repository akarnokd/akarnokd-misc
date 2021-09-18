package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

public class ShiftByCount {

    public static void main(String[] args) {
        Flowable.range(1, 7)
        .flatMap(v -> Flowable.timer(v * 200, TimeUnit.MILLISECONDS).map(w -> v))
        .doOnNext(v -> System.out.println(v))
        .publish(f ->
            f.skip(3).zipWith(f, (a, b) -> b).mergeWith(f.takeLast(3))
        )
        .blockingSubscribe(v -> System.out.println("<-- " + v))
        ;
    }
}
