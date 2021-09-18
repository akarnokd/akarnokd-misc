package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.reactivex.*;

public class RetryWhenManyTypeTest {


    @Test
    public void test1() {
        Single.timer(1, TimeUnit.SECONDS)
            .doOnSubscribe(s -> System.out.println("subscribing"))
            .map(v -> { throw new RuntimeException(); })
            .retryWhen(errors -> {
                AtomicInteger counter = new AtomicInteger();
                return errors
                          .takeWhile(e -> counter.getAndIncrement() != 3)
                          .flatMap(e -> {
                              System.out.println("delay retry by " + counter.get() + " second(s)");
                               return Flowable.timer(counter.get(), TimeUnit.SECONDS);
                          });
            })
            .blockingGet();
    }

    @Test
    public void test2() {
        Maybe.timer(1, TimeUnit.SECONDS)
            .doOnSubscribe(s -> System.out.println("subscribing"))
            .map(v -> { throw new RuntimeException(); })
            .retryWhen(errors -> {
                AtomicInteger counter = new AtomicInteger();
                return errors
                          .takeWhile(e -> counter.getAndIncrement() != 3)
                          .flatMap(e -> {
                              System.out.println("delay retry by " + counter.get() + " second(s)");
                               return Flowable.timer(counter.get(), TimeUnit.SECONDS);
                          });
            })
            .blockingGet();
    }

    @Test
    public void test3() {
        Completable.timer(1, TimeUnit.SECONDS)
            .doOnSubscribe(s -> System.out.println("subscribing"))
            .doOnComplete(() -> { throw new RuntimeException(); })
            .retryWhen(errors -> {
                AtomicInteger counter = new AtomicInteger();
                return errors
                          .takeWhile(e -> counter.getAndIncrement() != 3)
                          .flatMap(e -> {
                              System.out.println("delay retry by " + counter.get() + " second(s)");
                               return Flowable.timer(counter.get(), TimeUnit.SECONDS);
                          });
            })
            .blockingAwait();
    }
}
