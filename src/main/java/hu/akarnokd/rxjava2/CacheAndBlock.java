package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

public class CacheAndBlock {

    public static void main(String[] args) {
        Flowable<Integer> f1 = Flowable.just(1)
                .delaySubscription(1, TimeUnit.MILLISECONDS)
                .doOnNext(i -> {
                    System.out.println("f1: " + i);
                })
                .cache();
        Flowable<Integer> f2 = f1.map(i -> i + 1)
                .doOnNext(i -> {
                    System.out.println("f2: " + i);
                })
                .cache();
        Flowable<Integer> f3 = f1
                .map(i -> {
                    return f2.blockingFirst() + 1; // This is where it gets stuck
                })
                .doOnNext(i -> {
                    System.out.println("f3: " + i);
                });
        Flowable<Integer> f4 = f3
                .zipWith(f2, (f3Val, f2Val) -> 4)
                .doOnNext(i -> {
                    System.out.println("f4: " + i);
                });
        // If I use the version of f4 below, everything will work.
//      Flowable<Integer> f4 = f3
//              .concatMap(f3Val -> f2.map(f2Val -> 4))
//              .doOnNext(i -> {
//                  System.out.println("f4: " + i);
//              });
        System.out.println(f4.blockingFirst());
    }
}
