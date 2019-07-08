package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.functions.Function;

public class DebounceEmpty {
    @Test
    public void debounceOnEmpty() {
        Observable.empty().debounce(new Function<Object, ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> apply(Object o) {
                return Observable.just(new Object());
            }
        }).subscribe();
    }
}
