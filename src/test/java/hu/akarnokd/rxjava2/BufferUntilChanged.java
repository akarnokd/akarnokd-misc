package hu.akarnokd.rxjava2;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

public class BufferUntilChanged {

    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        Observable.fromArray(1,1,1,2,2,2,3,3,1,1,5,5)
        .compose(bufferUntilChanged(v -> v))
        .test()
        .assertResult(
                Arrays.asList(1, 1, 1),
                Arrays.asList(2, 2, 2),
                Arrays.asList(3, 3),
                Arrays.asList(1, 1),
                Arrays.asList(5, 5)
            );
    }

    static final <T, K> ObservableTransformer<T, List<T>> bufferUntilChanged(Function<T, K> keySelector) {
        return o -> o.publish(q -> q.buffer(q.distinctUntilChanged(keySelector).skip(1)));
    }

//    static final <T, K> ObservableTransformer<T, List<T>> bufferUntilChanged(Function<T, K> keySelector) {
//        return o ->
//            o.publish(q -> {
//                AtomicReference<K> lastKey = new AtomicReference<>();
//                return q.buffer(q.filter(v -> {
//                    K key = keySelector.apply(v);
//                    K last = lastKey.get();
//                    lastKey.lazySet(key);
//                    return last != null && !last.equals(key);
//                }));
//            });
//    }
}
