package hu.akarnokd.rxjava3;

import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SomeGroupBy1 {

    record Container(int partition, int item) { }
    
    @Test
    public void test() {
        var scheduler = Schedulers.from(Executors.newFixedThreadPool(5));
                Flowable.range(0, 5)
                        .flatMap(partition -> {
                            return Flowable.range(0, 10_000_000)
                                    .subscribeOn(Schedulers.io())
                                    .filter(item -> item % 5 == partition)
                                    .delay(10, TimeUnit.MILLISECONDS)
                                    .map(item -> new Container(partition, item))
                                    //.doOnNext(c -> System.out.println("  " + partition + " - " + c))
                                    ;
                        })
                        //.doOnNext(container -> System.out.println("Next :" + container.partition))
                        .groupBy(item -> item.partition)
                        /*
                        .flatMap(grouped -> {
                            System.out.println("+ Group " + grouped.getKey());
                            return grouped.flatMap(container -> {
                                return Flowable.fromAction(() -> {
                                    Thread.sleep(100);
                                    System.out.println("Partition " + container.partition + " " + container.item + " " + Thread.currentThread().getName());
                                })
                                .subscribeOn(scheduler);
                            }, 1);
                        })
                        */
                        .flatMap(grouped -> {
                            System.out.println("+ Group " + grouped.getKey());
                            return grouped.observeOn(scheduler).flatMap(container -> {
                                return Flowable.fromAction(() -> {
                                    Thread.sleep(100);
                                    System.out.println("Partition " + container.partition + " " + container.item + " " + Thread.currentThread().getName());
                                })
                                ;
                            }, 1);
                        })
                        .blockingSubscribe();
    }
}
