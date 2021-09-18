package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.internal.subscriptions.BooleanSubscription;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

public class LatestObserveOnTest {

    @Test
    public void test() throws Exception {
        for (int i = 0; i < 1000; i++) {
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            ts.onSubscribe(new BooleanSubscription());
            Flowable.range(1, 100)
            .onBackpressureLatest()
            .observeOn(Schedulers.io(), false, 1)
            .subscribe(v -> {
                Thread.sleep(100);
                ts.onNext(v);
            }, ts::onError, ts::onComplete);

            ts.awaitDone(5, TimeUnit.SECONDS)
            .assertResult(1, 100);
        }
        Thread.sleep(1000);
    }
}
