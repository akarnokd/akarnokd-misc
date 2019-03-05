package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class MergeCompletablesTest {
    public static void main(String[] args) throws InterruptedException {
        Observable<Completable> completables = Observable.range(1, 5).map(integer -> {

                return Completable.fromAction(() -> {
                    try {
                        System.out.println("Local Started");
                        Thread.sleep(3 * 1000);
                        System.out.println("Local Completed");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
                  .doOnDispose(() -> System.out.println("Local Unsubscribed: " + integer))
                  .doOnSubscribe(subscription -> System.out.println("Local Subscribed: " + integer));
        });

        Completable globalCompletable = Completable.merge(completables.toFlowable(BackpressureStrategy.MISSING), 10)
                .doOnDispose(() -> System.out.println("Global Unsubscribed")).timeout(1, TimeUnit.SECONDS);

        try {
            globalCompletable.blockingGet();
        } catch (Exception e) {
            System.out.println("Global Thread timeout: " + e);
        }

        Thread.sleep(20 * 1000);
    }
}
