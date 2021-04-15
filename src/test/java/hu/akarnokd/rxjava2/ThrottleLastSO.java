package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

public class ThrottleLastSO {

    @Test
    public void test() {
        TestScheduler ts = new TestScheduler();
        
        TestObserver<Object> to = Observable.error(new RuntimeException())
        .onErrorReturnItem(0)
        .startWith(-1)
        .throttleLast(50, TimeUnit.MILLISECONDS, ts)
        .test();
        
        ts.advanceTimeBy(1, TimeUnit.SECONDS);

        to.assertResult(0);
    }
}
