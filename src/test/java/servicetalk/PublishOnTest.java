package servicetalk;

import org.junit.Test;
import org.reactivestreams.*;

import io.servicetalk.concurrent.api.*;
import io.servicetalk.concurrent.api.Publisher;
import io.servicetalk.concurrent.reactivestreams.ReactiveStreamsAdapters;

public class PublishOnTest {

    @Test
    public void test() throws Throwable {

        Executor exec = Executors.newCachedThreadExecutor();
        Executor exec1 = Executors.newCachedThreadExecutor();

        org.reactivestreams.Publisher<Integer> p = ReactiveStreamsAdapters.toReactiveStreamsPublisher(
                Publisher.from(1, 2, 3, 4)
                .subscribeOn(exec)
                //.subscribeOnOverride(exec1)
        );
        try {
            try {
                p.subscribe(subscriber(-100000));

                Thread.sleep(2000);

                p.subscribe(subscriber(10));

                Thread.sleep(2000);
            } finally {
                exec.closeAsync().toFuture().get();
            }
        } finally {
            exec1.closeAsync().toFuture().get();
        }
    }

    static Subscriber<Integer> subscriber(long request) {
        return new Subscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(request);
            }

            @Override
            public void onNext(Integer t) {
                System.out.println(t);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println(Thread.currentThread());
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        };
    }
}
