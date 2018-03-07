package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class ConcatAnomalyTest {

    @Test
    public void test() throws Exception {
Flowable.concat(
    Flowable.just(1, 2)
    .concatMap(v -> Flowable.just(3, 4))
    .subscribeOn(Schedulers.single())
    .observeOn(Schedulers.io())
    .doOnNext(v -> System.out.println("[1]: " + v)),
    Single
    .just("yup")
    .subscribeOn(Schedulers.single())
    .doOnSuccess(o -> System.out.println("[2] called: " + o))
    .toFlowable()
)
.observeOn(Schedulers.computation())
.subscribe(v -> System.out.println("[3]: " + v), Throwable::printStackTrace, () -> System.out.println("Done"));

Thread.sleep(1000);
    }
}
