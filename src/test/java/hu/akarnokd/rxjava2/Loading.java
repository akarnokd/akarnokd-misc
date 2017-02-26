package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.*;

public final class Loading {

    private Loading() { }

    public static void main(String[] args) throws Exception {
        Subject<Object> loadingQueue = PublishSubject.<Object>create().toSerialized();

        loadingQueue
          .toFlowable(BackpressureStrategy.LATEST)
          .delay(0, TimeUnit.MILLISECONDS, Schedulers.single())
          .map(discarded -> {
            // PRE-LOADING
            System.out.println("PRE-LOADING: " + Thread.currentThread().getName());
            return discarded;
           })
          .delay(0, TimeUnit.MILLISECONDS, Schedulers.computation())
           .map(b -> {
               System.out.println("LOADING: " + Thread.currentThread().getName());
             Thread.sleep(2000);
             return b;
           })
           .delay(0, TimeUnit.MILLISECONDS, Schedulers.single())
           .rebatchRequests(1)
           .subscribe(b -> {
               System.out.println("FINISHED: " + Thread.currentThread().getName() + "\n\n");
           });


        loadingQueue.onNext(true);
        loadingQueue.onNext(true);
        loadingQueue.onNext(true);

        Thread.sleep(10000);
    }
}
