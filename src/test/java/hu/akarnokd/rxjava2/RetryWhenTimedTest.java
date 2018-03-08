package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;

public class RetryWhenTimedTest {

    public class RetryWhenObservable implements Func1<Observable<? extends Throwable>, Observable<?>> {

        static final String TAG = "RetryWhenObservable";
        int maxRetry, interval,retryCount = 0;

        public RetryWhenObservable(int maxRetry, int interval) {
            this.maxRetry = maxRetry;
            this.interval = interval;
        }
        @Override
        public Observable<?> call(Observable<? extends Throwable> attempts) {
            return attempts.flatMap(throwable -> {
                if (++retryCount < this.maxRetry) {
                    return Observable.timer(interval,TimeUnit.SECONDS);
                }else
                    return Observable.error(throwable);
            });
        }
    }

    @Test
    public void test() {
        Observable.error(new IOException())
        .retryWhen(new RetryWhenObservable(5, 1))
        .test()
        .awaitTerminalEvent()
        .assertFailure(IOException.class);
    }
}
