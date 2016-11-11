package hu.akarnokd.rxjava;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

public class ConcatWithTest2 {
    @Test
    public void concatWithTest() {
        Flowable.fromCallable(() -> {
            System.out.println("start");
            return "start";
        })
        .doOnError(e -> {
            System.out.println("doOnError");
        })
        .doOnComplete(() -> {
            System.out.println("doOnCompleted");
        })
        .concatWith(Flowable.fromCallable(() -> {
            System.out.println("concatWith");
            return "concatWith";
        }))
        .onErrorResumeNext(e -> {
            return Flowable.fromCallable(() -> {
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
