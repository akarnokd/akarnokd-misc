package hu.akarnokd.rxjava3;

import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SomeBlocking {

    @Test
    public void test() {
        Observable
        .range(1, 20)
        .flatMap(
                integer -> {
                    if (integer % 5 != 0) {
                        return Observable
                                .just(integer);
                    }

                    return Observable
                            .just(-integer)
                            .observeOn(Schedulers.io());
                },
                false,
                1
        )
        .ignoreElements()
        .blockingAwait();
    }
}
