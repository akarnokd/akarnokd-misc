package hu.akarnokd.rxjava2;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import io.reactivex.Observable;

public class CombineLatestError {

    @Test
    public void test() {
        final int result = Observable.combineLatestDelayError(
                Arrays.asList(
                        Observable.just(41).concatWith(Observable.error(new Exception("Failure"))),
                        Observable.just(1).delay(1, TimeUnit.SECONDS)
                ),
                ints -> {
                    System.out.println(Arrays.toString(ints));
                    return ((int) ints[0]) + ((int) ints[1]);
                }
        ).blockingFirst();

        Assert.assertEquals(42, result);
    }
}
