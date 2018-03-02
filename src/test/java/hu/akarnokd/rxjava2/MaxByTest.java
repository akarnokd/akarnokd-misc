package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Flowable;

public class MaxByTest {

    @Test
    public void test() {
        Flowable.fromArray("a", "bb", "ccc", "d", "eee", "ffff")
        .reduce((last, current) -> 
            Integer.compare(last.length(), current.length()) >= 0 
            ? last : current)
        .subscribe(System.out::println);
    }
}
