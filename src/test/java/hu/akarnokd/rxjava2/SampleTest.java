package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;

public class SampleTest {

    @Test
    public void test() throws Exception {

        Observable.interval(50, TimeUnit.MILLISECONDS)
        .doOnNext(v -> {
            if (v % 20 == 0) {
                System.out.println("--------- second: " + v / 20);
            }
        })
        .sample(200, TimeUnit.MILLISECONDS)
        .subscribe(System.out::println);

        Thread.sleep(5000);
    }
}
