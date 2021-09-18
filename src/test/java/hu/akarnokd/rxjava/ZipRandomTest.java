package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;

public class ZipRandomTest {

    static long randomTime() {
        return 2;
    }

    @Test
    public void test() throws Exception {
        Observable<String> delay = Observable.just("")
                .switchMap(dummy -> Observable.timer(randomTime(), TimeUnit.SECONDS))
                .map( a -> String.valueOf(a) )
                .repeat();

        Observable<String> messages = Observable.just("Test") //eventually lines from a file...
                .repeat();

        messages.zipWith(delay, (d, msg) -> ""+d+" "+msg  ).subscribe( System.out::println );

        Thread.sleep(10000);
    }
}
