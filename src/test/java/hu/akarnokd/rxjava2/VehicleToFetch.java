package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.*;

public class VehicleToFetch {

    @Test
    public void test() throws Exception {
        Subject<String> vehicleToFetch = PublishSubject.<String>create().toSerialized();
        vehicleToFetch
                .doOnNext(e -> System.out.println("Got: " + e))
                .delay(2,TimeUnit.SECONDS)
                .window(10, TimeUnit.SECONDS, 5, true)
                .observeOn(Schedulers.io())
                .subscribe(w-> w.toList().subscribe(ws-> {
                    System.out.println("New window: " + ws);
                    for (String v : ws) {
                        System.out.println(String.format("%s %d", v, Thread.currentThread().getId()));
                        vehicleToFetch.onNext(v);
                    };
                }));


        Observable.just("v1","v2","v3","v4")
        .concatWith(Observable.<String>never())
        .subscribe(vehicleToFetch);

        Thread.sleep(600000);
    }
}
