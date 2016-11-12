package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

public class ConcatWithTest3 {
    @Test
    public void concatWithTest() {
        Observable.fromCallable(() -> {
            System.out.println("start");
            return "start";
        })
        .doOnError(e -> {
            System.out.println("doOnError");
        })
        .doOnCompleted(() -> {
            System.out.println("doOnCompleted");
        })
        .concatWith(Observable.fromCallable(() -> {
            System.out.println("concatWith");
            return "concatWith";
        }))
        .onErrorResumeNext(e -> {
            return Observable.fromCallable(() -> {
                System.out.println("onErrorResumeNext");
                return "onErrorResumeNext";
            });
        })
        .flatMap(firstname -> {
            System.out.println("flatMap");
            throw new RuntimeException();
//            return Observable.just(firstname);
        })
        .subscribe(new TestSubscriber<>());
    }
}
