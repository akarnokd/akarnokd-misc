package hu.akarnokd.rxjava2;

import java.util.ArrayList;

import io.reactivex.Observable;

public class ReduceWhile {
    public static void main(String[] args) {
        Observable.range(1, 10)
        .scanWith(() -> new ArrayList<Integer>(), (a, b) -> { a.add(b); return a; })
        .takeUntil(a -> a.size() == 5)
        .takeLast(1)
        .subscribe(System.out::println);
    }
}
