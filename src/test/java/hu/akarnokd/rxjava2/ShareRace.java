package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

public class ShareRace {

    @Test
    public void test() {
for (int i = 0; i < 10_000; i++) {
    Observable<Integer> observable = Observable.just(1).publish().refCount();

    TestObserver<Integer> observer1 = observable
        .subscribeOn(Schedulers.computation())
        .test();

        TestObserver<Integer> observer2 = observable
        .subscribeOn(Schedulers.computation())
        .test();

    observer1
        .withTag("observer1 " + i)
        .awaitDone(5, TimeUnit.SECONDS)
        .assertNoErrors()
        .assertComplete();

    observer2
        .withTag("observer2 " + i)
        .awaitDone(5, TimeUnit.SECONDS)
        .assertNoErrors()
        .assertComplete();
}
    }
}
