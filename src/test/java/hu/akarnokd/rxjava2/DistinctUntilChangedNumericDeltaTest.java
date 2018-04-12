package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;

public class DistinctUntilChangedNumericDeltaTest {

    @Test
    public void test() {
        Observable.just(10.0, 10.1, 10.15, 10.162, 11.0)
        .distinctUntilChanged((prev, curr) -> Math.abs(prev - curr) < 0.09)
        .subscribe(System.out::println);
    }
}
