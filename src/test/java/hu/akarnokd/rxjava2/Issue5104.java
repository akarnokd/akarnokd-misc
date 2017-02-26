package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public final class Issue5104 {
    private Issue5104() { }

    public static void main(String[] args) throws Exception {
PublishSubject<String> vehicleToFetch = PublishSubject.create();
vehicleToFetch
        .delay(2,TimeUnit.SECONDS)
        .window(10, TimeUnit.SECONDS, 5)
        .observeOn(Schedulers.io())
        .subscribe(w -> w.toList().subscribe(ws -> {
            ws.forEach(v -> {
                System.out.println(String.format("%s %d", v, Thread.currentThread().getId()));
                vehicleToFetch.onNext(v);
            });
        }));


Observable.just("v1","v2","v3","v4").subscribe(v -> vehicleToFetch.onNext(v));

Thread.sleep(15000);
    }
}
