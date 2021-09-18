package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;

public class SwitchIfFewerTest {

    @Test
    public void test() {
        Observable.range(1, 5)
        .compose(switchIfFewer(Observable.range(1, 8), 10))
        .compose(switchIfFewer(Observable.range(1, 15), 10))
        .test()
        .assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    }

    static <T> ObservableTransformer<T, T> switchIfFewer(Observable<T> other, int n) {
        return o -> {
            return o.toList()
            .flatMapObservable(list -> {
                if (list.size() < n) {
                    return other;
                }
                return Observable.fromIterable(list);
            });
        };
    }
}
