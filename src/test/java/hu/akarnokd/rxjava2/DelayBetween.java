package hu.akarnokd.rxjava2;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;

public class DelayBetween {

    static Maybe<String> getId(String id) {
        return Maybe.just(id);
    }

    @Test
    public void test() {
        String[] ids = {"1", "2", "3", "4", "5", "6", "7", "8"};
        Flowable.fromArray(ids) // or fromIterable(ids)
        .zipWith(
                Flowable.interval(0, 5, TimeUnit.SECONDS).onBackpressureBuffer(),
                (id, time) -> id
            )
        .flatMapMaybe((String id) -> getId(id), false, 1)
        // .repeat()
        .blockingSubscribe((String v) -> {
            System.out.print(LocalTime.now());
            System.out.print(" - ");
            System.out.println(v);
        });
    }
}
