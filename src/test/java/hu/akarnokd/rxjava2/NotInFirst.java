package hu.akarnokd.rxjava2;

import java.util.HashSet;

import org.junit.Test;

import io.reactivex.Observable;

public class NotInFirst {

    @Test
    public void test() {
        Observable<Integer> obs1 = Observable.range(1, 10);
        Observable<Integer> obs2 = Observable.range(5, 10);

        obs1.collect(HashSet::new, (a, b) -> a.add(b))
        .flatMapObservable(set ->
            obs2.filter(set::contains)
        )
        .subscribe(System.out::println);
    }
}
