package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.Observable;
import rx.plugins.RxJavaHooks;

public class FirstAndThrowTest {

    @Test
    public void test() {
        RxJavaHooks.setOnError(error -> System.out.println("got global error " + error));
        Observable.just("1")
                .first()
                .toSingle()
                .subscribe(
                        it -> {
                            System.out.println("going to throw");
                            throw new NullPointerException("bla");
                        },
                        error -> System.out.println("got error " + error)
                );
    }
}
