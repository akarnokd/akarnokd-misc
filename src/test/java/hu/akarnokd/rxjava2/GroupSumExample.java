package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;

public class GroupSumExample {

    @Test
    public void test() {
        Observable.range(1, 20)
        .groupBy(v -> v % 2 == 0)
        .flatMapMaybe(group -> {
            return group.reduce((a, b) -> a + b)
                   .map(v -> "Group " + group.getKey() + " sum is " + v);
        })
        .subscribe(System.out::println);
    }
}
