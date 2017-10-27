package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class BlockingSingleTest {

    @Test
    public void test() {
        Observable<Object> source1 = Observable.fromCallable(() -> {
            throw new InterruptedException();
        }).subscribeOn(Schedulers.io());
        Observable<String> source2 = Observable.fromCallable(() -> "second_observable")
                .subscribeOn(Schedulers.io());
        Observable.zip(source1, source2, (o, s) -> "result").blockingSingle();
    }
}
