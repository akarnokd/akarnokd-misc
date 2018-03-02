package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;

public class ConcatMapDelayTest {
    @Test
    public void publisherOfPublisherDelayErrorX() {

        Flowable.concatDelayError(
                Flowable.just(
                        Flowable.just(1, 2),
                        Flowable.error(new Exception("test")),
                        Flowable.just(3, 4)))
        .test()
        .assertFailure(Exception.class, 1, 2, 3, 4);
    }
    @Test
    public void publisherOfPublisherDelayErrorX2() {

        Observable.concatDelayError(
                Observable.just(
                        Observable.just(1, 2),
                        Observable.error(new Exception("test")),
                        Observable.just(3, 4)))
        .test()
        .assertFailure(Exception.class, 1, 2, 3, 4);
    }
}
