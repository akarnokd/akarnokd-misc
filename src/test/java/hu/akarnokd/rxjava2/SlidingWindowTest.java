package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;

public class SlidingWindowTest {

    @Test
    public void test() {
        Observable.range(1, 4)
        .buffer(2, 1)
        .filter(list -> list.size() == 2)
        .map(list -> list.get(0) + list.get(1))
        .test()
        .assertResult(3, 5, 7);
    }
}
