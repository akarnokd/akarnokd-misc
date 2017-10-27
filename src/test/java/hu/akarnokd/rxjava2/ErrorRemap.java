package hu.akarnokd.rxjava2;

import java.io.IOException;

import org.junit.Test;

import io.reactivex.Observable;

public class ErrorRemap {

    @Test
    public void test() {
        Observable.error(new IOException())
        .onErrorResumeNext((Throwable e) -> Observable.error(new IllegalArgumentException()))
        .test()
        .assertFailure(IllegalArgumentException.class);
    }
}
