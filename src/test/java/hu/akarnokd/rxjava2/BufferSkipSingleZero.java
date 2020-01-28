package hu.akarnokd.rxjava2;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class BufferSkipSingleZero {
    static ObservableTransformer<Integer, Integer> skipSingleZero() {
        return source -> Observable.defer(() -> {
            AtomicInteger zerosSeen = new AtomicInteger();

            return source
            .flatMapIterable(item -> {
                if (item == 0) {
                    int n = zerosSeen.getAndIncrement();
                    if (n == 0) {
                        return Collections.emptyList();
                    }
                    if (n == 1) {
                        return Arrays.asList(0, 0);
                    }
                    return Collections.singletonList(0);
                }
                zerosSeen.set(0);
                return Collections.singletonList(item);
            });
        });
    }


    @Test
    public void test() {
    Observable<Integer> source = Observable.fromArray(
            0, 0, 0, 0, 0, 0, 0, 5, 5, 6, 7, 8, 8,
            0, 9, 10, 11, 4, 5, 6, 5, 0, 0, 0, 0, 0
    );

    source
    .compose(skipSingleZero())
    .test()
    .assertResult(
            0, 0, 0, 0, 0, 0, 0, 5, 5, 6, 7, 8, 8,
            9, 10, 11, 4, 5, 6, 5, 0, 0, 0, 0, 0
    );
    }

    @Test
    public void test2() {
    Observable<Integer> source = Observable.fromArray(
            0, 0, 0, 0, 0, 0, 0, 5, 5, 6, 7, 8, 8,
            0, 0, 9, 10, 11, 4, 5, 6, 5, 0, 0, 0, 0, 0
    );

    source
    .compose(skipSingleZero())
    .test()
    .assertResult(
            0, 0, 0, 0, 0, 0, 0, 5, 5, 6, 7, 8, 8,
            0, 0, 9, 10, 11, 4, 5, 6, 5, 0, 0, 0, 0, 0
    );
    }
}
