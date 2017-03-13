package hu.akarnokd.rxjava2;

import org.junit.*;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class TestObserverDispose {
    @Test
    public void observerShouldBeDisposed()  {
        TestObserver<String> observer = new TestObserver<>();
        Observable.just("").subscribe(observer);
        observer.awaitTerminalEvent();

        observer.assertNoErrors();
        observer.assertComplete();
        Assert.assertTrue(observer.isDisposed());
    }
}
