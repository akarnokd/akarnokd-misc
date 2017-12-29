package hu.akarnokd.reactivestreams.tck;

import java.util.concurrent.atomic.AtomicBoolean;

import org.reactivestreams.*;
import org.reactivestreams.tck.*;
import org.testng.annotations.Test;

@Test
public class EmptyPublisherTckTest extends PublisherVerification<Integer> {
    
    public EmptyPublisherTckTest() {
        super(new TestEnvironment(50));
    }

    @Override
    public Publisher<Integer> createPublisher(long elements) {
        return subscriber -> {
            AtomicBoolean cancelled = new AtomicBoolean();
            subscriber.onSubscribe(new Subscription() {

                @Override
                public void request(long n) {
//                    if (n <= 0 && !cancelled.get()) {
//                        subscriber.onError(new IllegalArgumentException("§3.9 violated"));
//                    }
                }

                @Override
                public void cancel() {
                    cancelled.set(true);
                }
            });
//            if (!cancelled.get()) {
                subscriber.onComplete();
//            }
        };
    }

    @Override
    public Publisher<Integer> createFailedPublisher() {
        return null;
    }

    @Override
    public long maxElementsFromPublisher() {
        return 0;
    }
}
