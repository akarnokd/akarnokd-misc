package hu.akarnokd.rxjava2;

import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class TimeoutTask {

    @Test
    public void test() {
        Observable<String> source = Observable.create(
                (ObservableEmitter<Callable<String>> emitter) -> {
            emitter.onNext(() -> task(0, "A"));
            emitter.onNext(() -> task(2, "B")); // this one times out
            emitter.onNext(() -> task(0, "C"));
            emitter.onNext(() -> task(0, "D"));
            emitter.onComplete();
        })
        .concatMap(call ->
            Observable.fromCallable(call)
               .subscribeOn(Schedulers.computation())
               .timeout(1, TimeUnit.SECONDS, Observable.just("timeout")) 
        );
        
        source
        .blockingSubscribe(s -> System.out.println("RECEIVED: " + s));
    }
    
    static String task(int i, String s) {
        return i + " " + s;
    }
}
