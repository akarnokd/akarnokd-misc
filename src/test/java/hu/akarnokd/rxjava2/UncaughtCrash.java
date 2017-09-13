package hu.akarnokd.rxjava2;

import java.io.IOException;

import org.junit.*;

import io.reactivex.plugins.RxJavaPlugins;

public class UncaughtCrash {
    
    @Before
    public void before() {
        RxJavaPlugins.setErrorHandler(e -> {
            Thread.currentThread().setUncaughtExceptionHandler((t, f) -> {
                Thread.currentThread().setUncaughtExceptionHandler(null);
                throw (InternalError)f;
            });
            throw new InternalError(e);
         });
    }

    @After
    public void after() {
        RxJavaPlugins.setErrorHandler(null);
    }

    @Test(expected = InternalError.class)
    public void test1() {
        RxJavaPlugins.onError(new IOException());
    }

    @Test(expected = InternalError.class)
    public void test2() {
        RxJavaPlugins.onError(new IOException());
    }
}
