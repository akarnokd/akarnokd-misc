package hu.akarnokd.rxjava2;

import org.junit.*;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import reactor.core.publisher.Flux;

public class PublishRefCountPublishTest {

    @Test
    public void test() {
        int[] calls = { 0 };
        
        Flowable.range(1, 10)
        .doOnCancel(() -> calls[0]++)
        .publish()
        .refCount()
        .publish(v -> v)
        .take(5)
        .test()
        .assertResult(1, 2, 3, 4, 5);
        
        Assert.assertEquals(1, calls[0]);
    }

    @Test
    public void test2() {
        int[] calls = { 0 };
        
        Flux.range(1, 10)
        .doOnCancel(() -> calls[0]++)
        .publish()
        .refCount()
        .publish(v -> v)
        .take(5)
        .subscribeWith(new TestSubscriber<Integer>())
        .assertResult(1, 2, 3, 4, 5);
        
        Assert.assertEquals(1, calls[0]);
    }
}
