package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ConcatEagerDebounceTest {

    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        Observable.concatArrayEagerDelayError(
                Observable.just(1).delay(100, TimeUnit.MILLISECONDS),
                Observable.<Integer>error(new IOException()).delay(200, TimeUnit.MILLISECONDS, true)
        )
        .onErrorResumeNext((Function<Throwable, Observable<Integer>>)(error -> Observable.<Integer>error(error).delay(500, TimeUnit.MILLISECONDS, true)))
        .doOnEach(System.out::println)
        .debounce(400, TimeUnit.MILLISECONDS)
        .blockingSubscribe(
                System.out::println,
                Throwable::printStackTrace
        );
    }
}
