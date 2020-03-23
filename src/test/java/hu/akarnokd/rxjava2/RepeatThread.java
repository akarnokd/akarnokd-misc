package hu.akarnokd.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class RepeatThread {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100; i++) {
            Flowable.range(1, 5)
            .repeat()
            .map(v -> {
                System.out.println(Thread.currentThread());
    //            Thread.sleep(100);
                return v;
            })
            .subscribeOn(Schedulers.single())
            .take(10)
            .blockingSubscribe();
        }
    }
}
