package hu.akarnokd.rxjava;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;

import rx.*;
import rx.observers.TestSubscriber;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

public class ProblemTestCase {
    Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    @After
    public void after() {
        RxJavaHooks.reset();
    }

    private Observable<String> createProblematicObservable() {
        return Observable.<String>fromCallable(() -> {
                throw new IllegalStateException();
            })
            .toSingle()
            .subscribeOn(scheduler)
            .toObservable()
            .onErrorResumeNext(caughtError -> {
                return Observable.just("OK");
            });
    }

    @Test
    public void testA() {
        TestSubscriber<Object> ts = new TestSubscriber<>();

        createProblematicObservable().subscribe(ts);

        ts.awaitTerminalEvent();

        ts.assertNoErrors();
        ts.assertValue("OK");
        ts.assertCompleted();
    }

    @Test
    public void testB() {
        AtomicBoolean isRxJavaHooksSetOnErrorCalled = new AtomicBoolean(false);
        RxJavaHooks.setOnError(throwable -> {
            isRxJavaHooksSetOnErrorCalled.set(true);
        });
        TestSubscriber<Object> ts = new TestSubscriber<>();

        createProblematicObservable().subscribe(ts);

        ts.awaitTerminalEvent();

        // We assert that RxJavaHooks.onError was *not* called, because Observable.onErrorResumeNext
        // should have been called.
        Assert.assertFalse(isRxJavaHooksSetOnErrorCalled.get());

        ts.assertNoErrors();
        ts.assertValue("OK");
        ts.assertCompleted();
    }
}