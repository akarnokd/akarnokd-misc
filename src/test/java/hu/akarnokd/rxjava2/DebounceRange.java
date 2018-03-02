package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;

public class DebounceRange {

    @Test
    public void test() {
        Observable.range(1, 10000)
        .debounce(1000, TimeUnit.MILLISECONDS)
        .blockingSubscribe(System.out::println);
    }
}
