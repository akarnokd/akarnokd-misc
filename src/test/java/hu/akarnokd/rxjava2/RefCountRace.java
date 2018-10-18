package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

public class RefCountRace {
    @Test
    public void replayRefCountShallBeThreadSafe() {
        for (int i = 0; i < 10000; i++) {
            Observable<Integer> observable = Observable.just(1).replay(1).refCount();

            TestObserver<Integer> observer1 = observable
                    .subscribeOn(Schedulers.io())
                    .test();

            TestObserver<Integer> observer2 = observable
                    .subscribeOn(Schedulers.io())
                    .test();

            observer1
            .withTag("" + i)
            .awaitDone(5, TimeUnit.SECONDS)
            .assertResult(1);

            observer2
            .withTag("" + i)
            .awaitDone(5, TimeUnit.SECONDS)
            .assertResult(1);
        }
    }
}
