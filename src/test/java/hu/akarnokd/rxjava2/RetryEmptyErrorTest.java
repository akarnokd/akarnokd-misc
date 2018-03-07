package hu.akarnokd.rxjava2;

import java.util.concurrent.atomic.*;

import org.junit.Test;

import io.reactivex.*;

public class RetryEmptyErrorTest {

    @Test
    public void emptyError() {
        AtomicInteger c = new AtomicInteger();
        Observable.fromCallable(() -> { throw new Exception("" + c.incrementAndGet()); })
        .compose(retryEmpty(2))
        .test()
        .assertFailureAndMessage(Exception.class, "2");
    }
    
    @Test
    public void nonEmptyError() {
        Observable.just(1).concatWith(Observable.error(new Exception()))
        .compose(retryEmpty(2))
        .take(4)
        .test()
        .assertResult(1, 1, 1, 1);
    }
    
    static <T> ObservableTransformer<T, T> retryEmpty(int count) {
        return o ->
            Observable.defer(() -> {
                AtomicInteger remaining = new AtomicInteger(count);
                AtomicBoolean nonEmpty = new AtomicBoolean();
                
                return o.doOnNext(v -> nonEmpty.lazySet(true))
                .retryWhen(err -> 
                    err.flatMap(e -> {
                        if (nonEmpty.get()) {
                            nonEmpty.lazySet(false);
                            remaining.lazySet(count);
                        } else
                        if (remaining.decrementAndGet() == 0) {
                            return Observable.error(e);
                        }
                        return Observable.just(1);
                    })
                 );
            });
    }
}
