package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;

public class Amb2Test {
    @Test
    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        Observable<Object> obs1 = Observable
                .<Object>just("obs 1 event")
                .doOnSubscribe(s -> System.out.println("obs1 sub"))
                .doOnDispose(() -> System.out.println("obs1 unsub"));

        Observable<Object> obs2 = Observable
                .<Object>just("obs 2 event")
                .doOnSubscribe(s -> System.out.println("obs2 sub"))
                .doOnDispose(() -> System.out.println("obs2 unsub"));

        Observable
        .ambArray(obs1, obs2)
        .subscribe(System.out::println);

        Thread.sleep(500);
    }
}
