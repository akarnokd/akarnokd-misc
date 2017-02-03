package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import rx.Observable;

public class DebounceFirst {

    public static void main(String[] args) {
        Observable.just(0, 100, 200, 1500, 1600, 1800, 2000, 10000)
        .flatMap(v -> Observable.timer(v, TimeUnit.MILLISECONDS).map(w -> v))
        .doOnNext(v -> System.out.println("T=" + v))
        .compose(debounceFirst(500, TimeUnit.MILLISECONDS))
        .toBlocking()
        .subscribe(v -> System.out.println("Debounced: " + v));
    }
    
    static <T> Observable.Transformer<T, T> debounceFirst(long timeout, TimeUnit unit) {
        return f -> 
            f.publish(g ->
                g.take(1)
                .concatWith(
                    g.switchMap(u -> Observable.timer(timeout, unit).map(w -> u))
                    .take(1)
                    .ignoreElements()
                )
                .repeatWhen(h -> h.takeUntil(g.ignoreElements()))
            )
            ;
    }
}
