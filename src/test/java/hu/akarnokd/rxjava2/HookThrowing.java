package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import org.junit.*;

import io.reactivex.Observable;
import io.reactivex.plugins.RxJavaPlugins;

public class HookThrowing {

    @Before
    public void before() {
        RxJavaPlugins.setErrorHandler(ex -> {
            UncaughtExceptionHandler h = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
                Thread.currentThread().setUncaughtExceptionHandler(h);
                HookThrowing.sneakyThrow(ex);
            });
            throw new RuntimeException("Fail up");
        });
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> void sneakyThrow(Throwable ex) throws E {
        throw (E)ex;
    }

    @After
    public void after() {
        RxJavaPlugins.reset();
    }

    @Test
    public void test() {
        Observable.error(new IOException())
        .subscribe();
    }
}
