package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;

public class FlatMapFatal {

    @Test(expected = UnknownError.class)
    public void test() {
        Observable.just(1)
        .flatMap(v -> { 
            throw new UnknownError(); 
        })
        .onErrorReturnItem(2)
        .test()
        .assertResult(2);
    }
}
