package hu.akarnokd.rxjava;

import rx.Observable;
import rx.schedulers.Schedulers;

public class Merge4691 {
    public static void main(String[] args) {
        Observable<Integer> o1 = Observable.just(1, 2, 3);
        Observable<Integer> o2 = Observable.merge(
            o1,
            Observable.empty()
        )
            .subscribeOn(Schedulers.newThread())
            .doOnCompleted(() -> error("a"));
        Observable<Integer> o3 = Observable.just(4, 5, 6);
        Observable<Integer> o4 = Observable.merge(
            o3,
            Observable.empty()
        )
            .subscribeOn(Schedulers.newThread())
            .doOnCompleted(() -> error("b"));
        Observable.merge(
            o2,
            o4
        )
            .doOnCompleted(() -> error("c"))
            .subscribe();
    }
    
    static void error(String s) {
        System.err.println(s);
    }
}
