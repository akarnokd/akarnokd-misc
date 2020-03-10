package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;

public class TimeoutExample {

    @Test
    public void test() {
        Observable<String> source = Observable.create(emitter -> {
            emitter.onNext("A");
            Thread.sleep(800);
            emitter.onNext("B");
            Thread.sleep(400);
            emitter.onNext("C");
            Thread.sleep(1200);
            emitter.onNext("D");
            emitter.onComplete();
        });
        source.timeout(1, TimeUnit.SECONDS)
                .subscribe(
                        item -> System.out.println("onNext: " + item),
                        error -> System.out.println("onError: " + error),
                        () -> System.out.println("onComplete will not be printed!"));
    }
}
