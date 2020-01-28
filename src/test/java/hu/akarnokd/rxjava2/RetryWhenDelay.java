package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;

public class RetryWhenDelay {

    @Test
    public void test() throws Exception {
        Observable<Integer> call = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onError(new Throwable("Error"));
        });

        call
        .retryWhen(throwableObservable -> throwableObservable.delay(1, TimeUnit.SECONDS))
        .subscribe(integer -> System.out.println(integer), throwable -> System.out.println(throwable.getMessage()));

        Thread.sleep(10000);
    }
}
