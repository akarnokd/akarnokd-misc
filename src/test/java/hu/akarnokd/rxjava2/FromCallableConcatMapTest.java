package hu.akarnokd.rxjava2;

import org.junit.Test;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class FromCallableConcatMapTest {

    @Test
    public void test() {
        BlockingScheduler bs = new BlockingScheduler();
        bs.execute(() -> {
            Observable.fromCallable(() -> Thread.currentThread())
            .concatMap(empty -> Observable.just(empty))
            .subscribeOn(Schedulers.io())
            .observeOn(bs)
            .doFinally(() -> bs.shutdown())
            .subscribe(System.out::println);
        });
    }
}
