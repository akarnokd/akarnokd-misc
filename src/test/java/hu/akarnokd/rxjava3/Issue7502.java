package hu.akarnokd.rxjava3;

import org.junit.Test;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Issue7502 {

    @Test
    public void test() {
        Flowable
        .just("")
        .repeat(5)
        .flatMap(singleIpHostAddress ->
            Flowable.create(o -> {
                throw new RuntimeException("test exception");
                /*
                if (!o.isCancelled()) {
                    o.onNext("");
                    o.onComplete();
                }
                */
            }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .retry(50)
        )
        .blockingSubscribe(v -> {
            System.out.println("onSuccess");
        },v -> {
            System.out.println("onError");
        });
    }
}
