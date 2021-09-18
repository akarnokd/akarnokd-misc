package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class OnErrorCrash {

    @Test
    public void test() throws Exception {
        Single.fromCallable(() -> { throw new RuntimeException(); })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        .subscribe(v -> { }, e -> { throw new RuntimeException(e); });

        Thread.sleep(50000);
    }
}
