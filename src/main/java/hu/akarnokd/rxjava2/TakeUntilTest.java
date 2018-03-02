package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;

public class TakeUntilTest {

    @Test
    public void test() {
        Observable.just(1,2,3,4,-1,1,2,3,4)
        .takeUntil(value -> value == -1)
        .subscribe(System.out::println);
    }
}
