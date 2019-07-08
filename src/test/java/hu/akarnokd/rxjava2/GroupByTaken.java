package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;

public class GroupByTaken {

    @Test
    public void test1() {
        Observable.interval(0, 1, TimeUnit.MILLISECONDS)
        .groupBy(n -> n % 2 == 0)
        .flatMapSingle(g -> {
            return g.take(10).toList();
        }).take(2).blockingForEach(System.out::println);
    }

    @Test
    public void test2() {
        Observable.interval(0, 1, TimeUnit.MILLISECONDS)
        .take(20)
        .groupBy(n -> n % 2 == 0)
        .flatMapSingle(g -> {
            return g.toList();
        })
        .blockingForEach(System.out::println);
    }
    
    @Test
    public void test3() {
        Observable.intervalRange(0, 20, 0, 1, TimeUnit.MILLISECONDS)
        .groupBy(n -> n % 2 == 0)
        .flatMapSingle(g -> {
            return g.toList();
        })
        .blockingForEach(System.out::println);
    }
}
