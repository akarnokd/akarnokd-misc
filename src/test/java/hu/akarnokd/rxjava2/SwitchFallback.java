package hu.akarnokd.rxjava2;

import java.util.List;

import io.reactivex.Observable;

public class SwitchFallback {

    public static void main(String[] args) {
        Observable<List<Integer>> source = Observable.empty();

                Observable<List<Integer>> fallbackSource = Observable.empty();

                source.flatMap(list -> {
                    if (list.isEmpty()) {
                        return fallbackSource;
                    }
                    return Observable.just(list);
                });
    }
}
