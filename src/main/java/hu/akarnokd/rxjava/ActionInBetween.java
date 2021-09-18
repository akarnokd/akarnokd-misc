package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;

public class ActionInBetween {

    public static void main(String[] args) throws Exception {
    PublishSubject<Integer> ps = PublishSubject.create();

    ps.publish(o ->
        o.mergeWith(
            o.switchMap(e ->
                 Observable.just(1).delay(200, TimeUnit.MILLISECONDS)
                .ignoreElements()
                .doOnCompleted(() -> System.out.println("Timeout action: " + e))
            )
        )
    ).subscribe(System.out::println);

    ps.onNext(1);
    ps.onNext(2);

    Thread.sleep(100);

    ps.onNext(3);

    Thread.sleep(250);

    ps.onNext(4);

    Thread.sleep(250);
    }
}
