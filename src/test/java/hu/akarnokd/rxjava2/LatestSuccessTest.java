package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.reactivex.*;

public class LatestSuccessTest {

    @Test
    public void test() {
        latestSuccess(Single.just(1), Single.error(new IOException()), Single.just(2))
        .test()
        .assertResult(2);
    }
    
    public static <T> Single<T> latestSuccess(Single<T>... sources) {
        return Single.defer(() -> {
            AtomicReference<T> last = new AtomicReference<T>();
            return Observable.fromArray(sources)
                .concatMap(source ->
                     source.doOnSuccess(last::lazySet)
                     .toObservable()
                     .onErrorResumeNext(Observable.empty())
                )
                .ignoreElements()
                .andThen(Single.fromCallable(() -> {
                    if (last.get() == null) {
                        throw new NoSuchElementException();
                    }
                    return last.get();
                }));
        });
   }
}
