package hu.akarnokd.rxjava2;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class ObservableConcatEagerTest {

    @Test
    public void test() {
        //Set<Integer> set = Ix.range(1, 1_000_000).toSet();
        
        Observable.range(1, 100000)
        .buffer(10)
        .concatMapEager(value -> Observable.just(value)
                .subscribeOn(Schedulers.io())
                .doOnNext(it -> Thread.sleep(ThreadLocalRandom.current().nextLong(10, 100))),
            3, 4)
        .blockingSubscribe(it -> System.out.println("Received : " + it));
    }
}
