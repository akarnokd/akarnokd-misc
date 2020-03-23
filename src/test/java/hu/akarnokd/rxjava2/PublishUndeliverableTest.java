package hu.akarnokd.rxjava2;

import java.io.IOException;

import org.junit.Test;

import io.reactivex.Observable;

public class PublishUndeliverableTest {

    @Test
    public void test() {
        Observable.error(new IOException())
        .publish(o -> {
            Observable<Object> cont = o.onErrorResumeNext((Throwable e) -> Observable.empty());
            return Observable.mergeDelayError(o, cont);
        })
        .onErrorResumeNext((Throwable e) -> Observable.just("bar"))
        .test()
        .assertResult("bar");
    }
}
