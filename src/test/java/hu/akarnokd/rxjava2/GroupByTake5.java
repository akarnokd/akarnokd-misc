package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;

public class GroupByTake5 {

    @Test
    public void test() {
        Observable.range(1, 50)
        .groupBy(v -> v / 10)
        .flatMap(group -> 
            group.publish(p -> p.take(5).mergeWith(p.ignoreElements()))
        )
        .subscribe(System.out::println);
    }
}
