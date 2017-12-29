package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class AdaptiveDebounce {

    @Test
    public void test() throws Exception {
        int DEBOUNCE_TIME = 100;
        AtomicInteger debounceTime = new AtomicInteger(DEBOUNCE_TIME);
        PublishSubject<Integer> mayRepeat = PublishSubject.create();

        AtomicInteger counter = new AtomicInteger();

        Observable<Integer> INTEGERS =
                Observable.fromArray(10, 20, 200, 250, 300, 550, 600, 650, 700, 1200)
                .flatMap(v -> Observable.timer(v, TimeUnit.MILLISECONDS)
                        .map(w -> counter.incrementAndGet()));

        INTEGERS.publish(o ->
                o.buffer(
                    Observable.defer(() ->
                        o.debounce(
                            debounceTime.get(), TimeUnit.MILLISECONDS)
                    )
                    .take(1)
                    .repeatWhen(v -> v.zipWith(mayRepeat, (a, b) -> b))
                )
            )
            .map(list -> {
                int nextDebounce = Math.min(100, list.size() * 100);
                debounceTime.set(nextDebounce);
                mayRepeat.onNext(1);
                return list;
            })
            .blockingSubscribe(System.out::println);
    }
}
