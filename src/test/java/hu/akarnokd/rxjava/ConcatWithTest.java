package hu.akarnokd.rxjava;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class ConcatWithTest {
    @Test
    public void concatWithTest() {
        Observable.fromCallable(() -> {
            System.out.println("start");
            return "start";
        })
        .doOnError(e -> {
            System.out.println("doOnError");
        })
        .doOnComplete(() -> {
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
        .subscribe(new TestObserver<>());
    }
}
