package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class GenerateExampleTest {

    @Test
    public void test() {
        Flowable.<Single<Integer>, Integer>generate(() -> 0, (s, e) -> {
            e.onNext(Single.just(s + 2).subscribeOn(Schedulers.computation()));
            if (++s == 5) {
                e.onComplete();
            }
            return s;
         })
         .flatMapSingle(v -> v, false, 1)
         .test()
         .awaitDone(5, TimeUnit.SECONDS)
         .assertResult(2, 3, 4, 5, 6);
    }
}
