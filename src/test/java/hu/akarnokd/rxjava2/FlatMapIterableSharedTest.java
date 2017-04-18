package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Flowable;
import ix.Ix;

public class FlatMapIterableSharedTest {

    @Test
    public void test() {
        Flowable.just(Ix.range(0, 300), Ix.range(0, 300))
        .flatMapIterable(x->x)
        .doOnNext(System.out::println)
        .share()
        .share()
        .count()
        .doOnEvent((a, b) -> {
            if (a != null) {
                System.out.println(a);
            } else {
                b.printStackTrace();
            }
        })
        .test()
        .assertResult(600L);
    }

    @Test
    public void test2() {
        Flowable.just(Ix.range(0, 600))
        .flatMapIterable(x->x)
        .doOnNext(System.out::println)
        .share()
        .share()
        .count()
        .doOnEvent((a, b) -> {
            if (a != null) {
                System.out.println(a);
            } else {
                b.printStackTrace();
            }
        })
        .test()
        .assertResult(600L);
    }
}
