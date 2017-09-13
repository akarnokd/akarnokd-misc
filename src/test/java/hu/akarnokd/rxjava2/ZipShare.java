package hu.akarnokd.rxjava2;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import io.reactivex.Observable;

public class ZipShare {

    @Test
    public void test() {
        Observable myObservable = Observable.just(1)
                .flatMap(i -> {
                    throw new IllegalStateException();
                }).share();

            myObservable
                .zipWith(myObservable, Pair::of)
                .subscribe(pair -> {
                    //ignore
                }, throwable -> {
                    //ignore
                });
    }
}
