package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Timeout800Test {

    @Test
    public void test() throws Exception {
Observable.<Integer>fromCallable(() -> {
    Thread.sleep(1000);
    return 1;
})
.timeout(800, TimeUnit.MILLISECONDS)
.subscribeOn(Schedulers.io())
.onErrorReturn(throwable -> {
    System.out.printf("Server did not respond within %s ms for id=%s%n", 800, 1);
    return 2;
})
.subscribe();

Thread.sleep(2000);
    }
}
