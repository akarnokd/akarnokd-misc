package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.*;

public class ConcatReplayTest {

    @Test
    public void test() {
        Observable<Integer> obs1 = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);

        Observable<Integer> obs2 = Observable.create(emitter -> {
            emitter.onNext(2);
            emitter.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);

        Observable<Integer> both = Observable
                .concatDelayError(obs1, obs2)
                .replay().autoConnect();

        both.test().assertResult(1, 2);

        both.test().assertResult(1, 2);
    }
}
