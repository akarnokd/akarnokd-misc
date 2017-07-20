package hu.akarnokd.rxjava2;

import java.util.*;

import io.reactivex.Observable;

public class InsertHeader {

    public static void main(String[] args) {
    Observable.just(1, 3, 4, 5, 7, 8, 10, 12, 14)
    .buffer(2, 1)
    .flatMapIterable(v -> {
        if (v.size() <= 1) {
            return v;
        }
        if (v.get(1) - v.get(0) > 1) {
            return Arrays.asList(v.get(0), 1000);
        }
        return Collections.singletonList(v.get(0));
    })
    .subscribe(System.out::println);
    }
}
