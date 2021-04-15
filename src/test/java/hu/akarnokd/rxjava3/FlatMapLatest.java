package hu.akarnokd.rxjava3;

import java.util.concurrent.Executors;

import org.junit.Test;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class FlatMapLatest {
    @Test
    public void someTest(){
        BehaviorSubject<Integer> filterEmmiter = BehaviorSubject.create();
        Flowable<Integer> filter = filterEmmiter.toFlowable(BackpressureStrategy.LATEST);
        final int maxint = 10;
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (int i = 0; i <= maxint; i++) {
                filterEmmiter.onNext(i);
            }
        }).start();

        filter
            .flatMap(integer -> {
            return Flowable.fromCallable(() -> {
                Thread.sleep(1000);
                System.out.println("TestTime value processed:"+integer);
                return integer;
            })
            .subscribeOn(Schedulers.io());
        }, false, 1, 1)
                .observeOn(Schedulers.from(Executors.newCachedThreadPool()), false, 1)
                .filter(value -> value==maxint).blockingFirst();
    }
}
