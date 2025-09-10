package hu.akarnokd.rxjava3;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.*;

public class DebounceAndBufferWithTime {

    public static 
void main(String[] args) throws Throwable {
    
    var source = //Observable.empty();
    Observable.fromArray(1, 2, 3)
    .concatWith(Observable.range(10, 25))
    .concatWith(Observable.range(45, 4))
    .concatWith(Observable.range(55, 25))
    .flatMap(v -> Observable.just(v).delay(v, TimeUnit.SECONDS))
    .doOnNext(v -> System.out.println("Tick - " + v))
    .doOnComplete(() -> System.out.println("Source done"))
    ;
    
    source.publish(shared -> {
        var db = shared
                .doOnEach(e -> System.out.println("debounce " + e))
                .debounce(5, TimeUnit.SECONDS)
                .doOnNext(v -> System.out.println("Debounce 5 seconds - " + v))
                .publish()
                .autoConnect()
                ;
        
        var stop = new AtomicBoolean(true);
        
        var wnd = shared
                .doOnEach(e -> System.out.println("window " + e))
                .take(1)
                .doOnNext(v -> stop.set(false))
                .delay(20, TimeUnit.SECONDS)
                .takeUntil(db.doOnNext(v -> stop.set(false)))
                .repeatUntil(() -> stop.getAndSet(true))
                .doOnNext(v -> System.out.println("Window 20 seconds " + v))
                ;
        
        return shared
                .doOnEach(e -> System.out.println("publish-shared " + e))
                .buffer(db.mergeWith(wnd)
                        .doOnEach(e -> System.out.println("Merge " + e))
                )
                .doOnEach(e -> System.out.println("Buffer " + e))
                ;
    })
    .blockingSubscribe(System.out::println);
    
    Thread.sleep(1000);
}
}
