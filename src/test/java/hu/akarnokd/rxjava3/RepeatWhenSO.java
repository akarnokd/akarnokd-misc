package hu.akarnokd.rxjava3;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.TestScheduler;

public class RepeatWhenSO {

    @Test
    public void test() {
        TestScheduler testScheduler = new TestScheduler();

        AtomicInteger counter = new AtomicInteger();

        TestObserver<String> to = Observable.<String>fromCallable(() -> {
            if (counter.getAndIncrement() == 0) {
                return "PENDING";
            }
            return "ELIGIBLE";
        })
        .repeatWhen(o -> o.delay(5, TimeUnit.SECONDS, testScheduler))
        .takeUntil (item ->
        !item.equals("PENDING")
                ).doOnNext(item ->
                System.out.println("doOnNext called")
                        )
        .lastElement()
        .toSingle()
        .test();

        to.assertEmpty();

        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        to.assertResult("ELIGIBLE");
    }
}
