package hu.akarnokd.rxjava;

import java.util.concurrent.Executors;

import org.junit.Test;

import rx.*;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FromCallCancel {
    Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    @Test
    public void testLoop() {
        for (int i = 0; i < 100000; i++) {
            test();
        }
    }

    @Test
    public void test() {

        Subscription subscription1 = Completable.fromCallable(() -> {
          Thread.sleep(1000);
          System.out.println("first Callable executed");
          return 0;
        })
            .subscribeOn(scheduler)
            .subscribe();

        Subscription subscription2 = Completable.fromCallable(() -> {
          Thread.sleep(1000);
          System.out.println("second Callable executed");
          return 0;
        })
            .subscribeOn(scheduler)
            .subscribe();

        CompositeSubscription subscriptions = new CompositeSubscription();
        subscriptions.addAll(subscription1, subscription2);
        subscriptions.clear();
    }
}
