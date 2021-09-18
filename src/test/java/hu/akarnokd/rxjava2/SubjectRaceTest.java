package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.*;

public class SubjectRaceTest {

    private final Subject<String> singlePropertyUpdateSubject =
            ReplaySubject.<String>create().toSerialized();


    public static void main(String[] args) {
        SubjectRaceTest obj= new SubjectRaceTest();
        obj.sendEvents();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


//[1]
    private final String valueToObserve = "2000";
    private void subscribeToSubject() {
        System.out.println("Subscribing .....");
        io.reactivex.Observable.range(1,10).subscribeOn(Schedulers.newThread()).subscribe(
                value -> getAndObserve(valueToObserve)
                .subscribe(observedValue -> System.out.println("  Value Received   "+observedValue +" By "+Thread.currentThread() ))
        );



    }

    private io.reactivex.Observable<String> getAndObserve(String value) {
        final io.reactivex.Observable<String> observable = singlePropertyUpdateSubject
                //.doOnNext(v-> System.out.println("Received value "+v))
                .filter(v -> v.equals(value))
                .doOnSubscribe(c-> System.out.println("Consumer subscribed "+c));
        return observable;
    }


// 50ms >= expected result ;  Anything less than 10ms will fail.
    private void sendEvents() {
        io.reactivex.Observable.interval(1, TimeUnit.MILLISECONDS)
        .takeUntil((Predicate<Long>)(key) -> key.toString().equals(valueToObserve))
        .subscribe(value -> {
            String key = value.toString();
            //System.out.println("Adding key "+key);
            singlePropertyUpdateSubject.onNext(key);
//[2]
 if (value == 1998){
                subscribeToSubject();;
            }
            if (value%100==0) {
                System.out.println(value);
            }
        });
    }
}
