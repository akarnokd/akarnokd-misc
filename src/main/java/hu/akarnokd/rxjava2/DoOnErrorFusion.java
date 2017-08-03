package hu.akarnokd.rxjava2;

import org.junit.Assert;

import io.reactivex.Flowable;
import io.reactivex.exceptions.CompositeException;
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
                i -> Assert.fail(), 
                e -> e.printStackTrace());
        f.connect();
    }
}
