package servicetalk;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.*;
import org.testng.annotations.*;

import io.servicetalk.concurrent.api.*;
import io.servicetalk.concurrent.reactivestreams.ReactiveStreamsAdapters;

@Test
public class PublishOnOverrideTckTest extends PublisherVerification<Integer> {

    static Executor exec;
    static Executor exec0;

    PublishOnOverrideTckTest() {
        super(new TestEnvironment(25));
    }

    @BeforeClass
    public static void before() {
        exec = Executors.newCachedThreadExecutor();
        exec0 = Executors.newCachedThreadExecutor();
    }

    @AfterClass
    public static void after() {
        try {
            exec.closeAsync().toFuture().get();
        } catch (Throwable ignored) {
        }
        try {
            exec0.closeAsync().toFuture().get();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public Publisher<Integer> createPublisher(long elements) {
        return ReactiveStreamsAdapters.toReactiveStreamsPublisher(
                io.servicetalk.concurrent.api.Publisher.from(items(elements))
                .publishOn(exec0)
                .publishOn(exec)
        );
    }

    static Integer[] items(long count) {
        int c = (int)count;
        Integer[] result = new Integer[c];
        for (int i = 0; i < c; i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    public Publisher<Integer> createFailedPublisher() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long maxElementsFromPublisher() {
        return 1024;
    }
}
