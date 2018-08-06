package hu.akarnokd.rxjava2;

import java.util.List;

import io.reactivex.Observable;

public class SwitchFallback {

    public static void main(String[] args) {
        Observable<List<Integer>> source = null;

                Observable<List<Integer>> fallbackSource = null;

                source.flatMap(list -> {
                    if (list.isEmpty()) {
                        return fallbackSource;
                    }
                    return Observable.just(list);
                });
    }
}
