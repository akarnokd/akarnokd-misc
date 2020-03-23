package hu.akarnokd.rxjava2;

import java.io.IOException;

import org.junit.Test;

import rx.Observable;

public class RedoCountTest {

    @Test
    public void retryZero() {
        Observable.range(1, 5).concatWith(Observable.<Integer>error(new IOException()))
        .retry(0)
        .test()
        .assertFailure(IOException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void retryOne() {
        Observable.range(1, 5).concatWith(Observable.<Integer>error(new IOException()))
        .retry(1)
        .test()
        .assertFailure(IOException.class, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5);
    }

    @Test
    public void repeatZero() {
        Observable.range(1, 5)
        .repeat(0)
        .test()
        .assertResult();
    }

    @Test
    public void repeatOne() {
        Observable.range(1, 5)
        .repeat(1)
        .test()
        .assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void repeatTwo() {
        Observable.range(1, 5)
        .repeat(2)
        .test()
        .assertResult(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);
    }
}
