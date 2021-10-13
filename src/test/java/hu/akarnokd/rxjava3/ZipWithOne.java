package hu.akarnokd.rxjava3;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.rxjava3.core.Flowable;

public class ZipWithOne {

    @Test
    public void test() {
        Flowable<Integer> integerFlowable = Flowable.just(1, 2, 3);
        Flowable<Integer> delayedFlowable = Flowable.just(10).delay(1, TimeUnit.SECONDS).cache();

        integerFlowable.concatMap(main -> delayedFlowable.map(other -> Integer.sum(main, other)))
                      .test()
                      .awaitDone(5, TimeUnit.SECONDS)
                      .assertResult(11, 12, 13);
    }
}
