package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Flowable;

public class BlockingSubscribeOrdering {

    @Test
    public void test() {
        Flowable.just(1, 2)
        .concatWith(Flowable.error(new Exception("whoops")))
        .blockingSubscribe(v -> System.out.println("received: " + v), 
                e -> {
                    e.printStackTrace(System.out);
                }, () -> System.out.println("completed"));

    }
}
