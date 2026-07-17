package hu.akarnokd.rxjava3;

import io.reactivex.Observable;
import io.reactivex.rxjava3.core.Flowable;

public class Issue7956 {

    public static void main(String[] args) {
        final var content = Flowable
                .rangeLong(0, 100)
                .window(10)
                //.flatMapSingle(Flowable::toList, false, 1);
                .concatMapEager(v -> v.toList().toFlowable(), 1, 1);

        System.out.println("Example 1: Expected behavior for concetMapSingle(Flowable::toList)");
        content
                .doOnNext(System.out::println)
                .blockingSubscribe();

        System.out.println("Example 2: Window abandonment & data-loss");
        content
                .doOnNext(v -> {
                    System.out.println("DoOnNext: " + v);
                })
                .blockingSubscribe(System.out::println);

        /*
        Observable.rangeLong(0, 100)
        .window(10)
        .concatMapSingle(Observable::toList)
        .blockingSubscribe(System.out::println);
        */
    }
}
