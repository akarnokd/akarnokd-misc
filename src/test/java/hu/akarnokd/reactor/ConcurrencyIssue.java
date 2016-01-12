package hu.akarnokd.reactor;

import org.junit.Test;

import reactivestreams.commons.publisher.*;
import reactivestreams.commons.subscriber.test.TestSubscriber;
import reactor.Processors;
import reactor.core.processor.FluxProcessor;
import reactor.core.subscription.ReactiveSession;

public class ConcurrencyIssue {
    
    @Test
    public void concurrentTimeoutErrorLoop() {
        for (int i = 0; i < 100; i++) {
            concurrentTimeoutError();
        }
    }
    
    @Test
    public void concurrentTimeoutError() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();

        FluxProcessor<Integer, Integer> p = Processors.topic();
        FluxProcessor<Integer, Integer> p2 = Processors.topic("test", 5);

        p2.subscribe(ts);

        ReactiveSession<Integer> s = p.startSession();
        new PublisherTake<>(p, 100)
//                .doOnRequest(r -> System.out.println("-- " + r))
//                .doOnNext(System.out::println)
                .timeout(PublisherNever.instance(), v -> PublisherNever.instance())
                .subscribe(p2);
//        p.subscribe(p2);


        for(int i = 0; i < 100; i++) {
            s.submit(1);
        }
        p.onComplete();

        ts.await();
        ts.assertValueCount(100).assertComplete();
    }
}
