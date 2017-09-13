package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;

public class JustFlatMapCallableNull {

    @Test
    public void test() {
        Observable.just(1).flatMap(i -> Observable.fromCallable(() -> null))
        .test()
        .assertFailure(NullPointerException.class);
    }

    @Test
    public void test1() {
        Observable.just(1).hide().flatMap(i -> Observable.fromCallable(() -> null))
        .test()
        .assertFailure(NullPointerException.class);
    }

    @Test
    public void test2() {
        Flowable.just(1).flatMap(i -> Flowable.fromCallable(() -> null))
        .test()
        .assertFailure(NullPointerException.class);
    }
}
