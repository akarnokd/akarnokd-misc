package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;

public class ItemTestSO {

    static class ItemTest {
        int i;
        ItemTest(int i) {
            this.i = i;
        }
        @Override
        public String toString() {
            return "ItemTest " + i;
        }
    }

    @Test
    public void itemTest() {
        Observable.zip(Observable.interval(1, TimeUnit.SECONDS),
                Observable.range(1, 10).map(v -> new ItemTest(v))
                ,
                (a, b) -> b, false, 1)
        .concatWith(Observable.<ItemTest>empty()
                .doOnSubscribe(s -> System.out.println("<empty>"))
                .delay(10, TimeUnit.SECONDS))
        .blockingSubscribe(System.out::println);
    }
}
