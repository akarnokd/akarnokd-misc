package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.functions.Predicate;

public class MatchIndex {

    @Test
    public void test() {
        Observable.range(1, 10)
        .compose(indexOf(v -> v == 5))
        .subscribe(System.out::println);
        
        Observable.range(1, 10)
        .compose(indexOf(v -> v == 12))
        .subscribe(System.out::println);
    }

    static <T> ObservableTransformer<T, Integer> indexOf(Predicate<? super T> predicate) {
        return o -> Observable.defer(() -> {
            int[] index = { -1, -1 };
            return o.filter(v -> {
                index[0]++;
                if (predicate.test(v)) {
                    index[1] = index[0];
                    return true;
                }
                return false;
            })
            .takeWhile(v -> index[1] != -1)
            .ignoreElements()
            .andThen(Observable.fromCallable(() -> index[1]));
        });
    }
}
