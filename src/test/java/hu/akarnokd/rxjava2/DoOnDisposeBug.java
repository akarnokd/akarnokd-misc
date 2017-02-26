package hu.akarnokd.rxjava2;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;

import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;

public class DoOnDisposeBug {
    @Test
    public void test() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();

        Assert.assertFalse(atomicBoolean.get());

        TestObserver<Void> to = Completable.complete()
            .doOnDispose(() -> atomicBoolean.set(true))
            .test()
            .assertResult();

        to
            .dispose();

        Assert.assertTrue(atomicBoolean.get()); // Fails
    }
}
