package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import rx.Single;

public class SingleAsync {

    public static void main(String[] args) throws Exception {
        Single.just("test")
        .delay(1, TimeUnit.MILLISECONDS)
        .subscribe(
        result -> {
            System.out.println("got text: " + result);
            throw new IllegalStateException("single async something is wrong");
            // onError will not be called, hard crash
        },
        e -> {
            // not called
            System.out.println("caught error " + e.getMessage());
            e.printStackTrace();
        });

        Thread.sleep(2000);
    }
}
