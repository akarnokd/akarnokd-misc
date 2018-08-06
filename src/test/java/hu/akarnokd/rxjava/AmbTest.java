package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.Observable;

public class AmbTest {

    @Test
    public void test() throws Exception {
        Observable<Object> obs1 = Observable
                .<Object>just("obs 1 event")
                .doOnSubscribe(() -> System.out.println("obs1 sub"))
                .doOnUnsubscribe(() -> System.out.println("obs1 unsub"));

        Observable<Object> obs2 = Observable
                .<Object>just("obs 2 event")
                .doOnSubscribe(() -> System.out.println("obs2 sub"))
                .doOnUnsubscribe(() -> System.out.println("obs2 unsub"));

        Observable
                .amb(obs1, obs2)
                .subscribe(System.out::println);

        Thread.sleep(500);
    }
}
