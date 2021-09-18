package hu.akarnokd.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;

public class DoOnErrorFusion {

    public static void main(String[] args) {
        ConnectableFlowable<Integer> f = Flowable.just(1)
                .doOnNext(i -> {
                    throw new IllegalArgumentException();
                })
                .doOnError(e -> {
                    throw new IllegalStateException(e);
                }).publish();
        f.subscribe(
                i -> { throw new AssertionError(); },
                e -> e.printStackTrace());
        f.connect();
    }
}
