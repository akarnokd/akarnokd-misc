package hu.akarnokd.rxjava2;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.internal.subscriptions.BooleanSubscription;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.SerializedSubscriber;

public class SwitchMapRace {

    AtomicInteger outer = new AtomicInteger();
    AtomicInteger inner = new AtomicInteger();

    @Test
    public void test() throws Throwable {
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            Flowable.<Integer>create(it -> {
                it.onNext(0);
            }, BackpressureStrategy.MISSING)
                .switchMap(v -> createFlowable())
                .observeOn(Schedulers.computation())
                .doFinally(() -> {
                    System.out.println("Outer finally: " + outer.incrementAndGet());
                })
                .take(1)
                .blockingSubscribe(v -> { }, Throwable::printStackTrace);
        }

        Thread.sleep(1000);
        assertEquals(inner.get(), outer.get());
        assertEquals(n, inner.get());
    }

    Flowable<Integer> createFlowable() {
        return Flowable.<Integer>unsafeCreate(s -> {
            SerializedSubscriber<Integer> it = new SerializedSubscriber<>(s);
            it.onSubscribe(new BooleanSubscription());
            Schedulers.io().scheduleDirect(() -> {
                it.onNext(1);
            }, 0, TimeUnit.MILLISECONDS);
            /*
            Schedulers.io().scheduleDirect(() -> {
                it.onNext(2);
            }, 0, TimeUnit.MILLISECONDS);
            */
            it.onNext(2);
        })
            .doFinally(() -> {
                System.out.println("Inner finally: " + inner.incrementAndGet());
            });
    }

    /*
    Flowable<Integer> createFlowable() {
        return Flowable
            .<Integer>create(it0 -> {
                FlowableEmitter<Integer> it = it0.serialize();
                Schedulers.io().scheduleDirect(() -> {
                    it.onNext(1);
                }, 0, TimeUnit.MILLISECONDS);
                Schedulers.io().scheduleDirect(() -> {
                    it.onNext(2);
                }, 0, TimeUnit.MILLISECONDS);
            }, BackpressureStrategy.BUFFER)
            .doFinally(() -> {
                System.out.println("Inner finally: " + inner.incrementAndGet());
            });
    }
    */
}
