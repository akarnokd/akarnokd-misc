package hu.akarnokd.rxjava2;

import org.junit.Test;

import rx.Observable;

public class GroupByFilterTest {
    @Test
    public void test() {
        Observable.range(1, 100)
                .groupBy(n -> n % 3)
                .toMap(g -> g.getKey())
                .flatMap(m ->  m.get(0))
                .subscribe(System.out::println);
    }
}
