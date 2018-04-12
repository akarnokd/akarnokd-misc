package hu.akarnokd.rxjava2;

import java.io.IOException;

import org.junit.Test;

import io.reactivex.*;

public class DoOnErrorInside {

    @Test
    public void test() {
        Observable.just(1)
        .flatMap(v -> single(v)
                .toObservable()
                .doOnError(w -> System.out.println("Error2 " + w))
        )
        .subscribe(v -> System.out.println(v), e -> System.out.println("Error " + e));
    }
    
    Single<Integer> single(Integer v) {
        return Single.error(new IOException());
    }
}
