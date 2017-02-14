package hu.akarnokd.rxjava2;
import java.util.Collections;

import io.reactivex.*;

public class RxJava2Test {
    public static void main(String... args) {
        Flowable<Integer> source = Flowable.create((e) -> {
            for (int i = 0; i < 300; i++) {
                e.onNext(i);
            }
            e.onComplete();
        }, BackpressureStrategy.BUFFER);
        Flowable<Integer> cached = source.cache();

        long sourceCount = source.concatMapIterable(Collections::singleton)
                .distinct().count().blockingGet();
        long cachedCount = cached.concatMapIterable(Collections::singleton)
                .distinct().count().blockingGet();

        System.out.println("source: " + sourceCount);
        System.out.println("cached: " + cachedCount);
    }
}