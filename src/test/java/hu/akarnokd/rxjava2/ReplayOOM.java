package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Flowable;

public class ReplayOOM {

    static int i;

    @Test
    public void test() {
        Flowable.range(1, 5000)
        .map(__ -> new byte[1024 * 1024])
        .replay(
          fb ->
            fb.take(1)
              .concatMap(__ -> fb)
          ,1
        )
        .doOnNext(v -> {
            if (i++ == 1000) {
                System.out.println("");
            }
        })
        .count()
        .toFlowable()
        .blockingSubscribe(c -> System.out.println("args = [" + c + "]"));
    }
}
