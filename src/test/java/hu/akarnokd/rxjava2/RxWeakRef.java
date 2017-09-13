package hu.akarnokd.rxjava2;

import java.lang.ref.WeakReference;

import org.junit.Test;

import io.reactivex.Observable;

public class RxWeakRef {

    @Test
    public void test() throws Exception {
        WeakReference<Integer> wr = new WeakReference<>(123456789);

        Observable<Integer> obs = Observable.fromCallable(() -> wr.get());

        System.gc();
        Thread.sleep(200);

        wr.getClass();

        obs.test()
        .assertFailure(NullPointerException.class);
    }
}
