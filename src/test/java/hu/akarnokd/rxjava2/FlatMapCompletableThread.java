package hu.akarnokd.rxjava2;


import java.util.*;

import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class FlatMapCompletableThread {

    @Test
    public void test() {
        for (int j = 0; j < 1; j++) {
            List<String> writerObj = new ArrayList<>();

            Observable.range(0, 1000)
                    .map(i -> Observable.just("hello world"))
                    .flatMap(obs -> obs
                            .flatMapCompletable(elem -> {
                                    writerObj.add(elem);
                                    System.out.println(Thread.currentThread().getName()  + " executing");
                                    return Completable.complete();
                            })
                            .toObservable()
                    .subscribeOn(Schedulers.io()))
                    .blockingSubscribe();
            //Size of the list is not always 1000
            System.out.println("The size of the list is : " + writerObj.size());
        }
    }
}
