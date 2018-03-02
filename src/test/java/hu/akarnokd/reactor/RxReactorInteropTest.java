package hu.akarnokd.reactor;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.reactivestreams.Publisher;

import reactor.core.publisher.*;
import rx.*;

public class RxReactorInteropTest {
    @Test
    public void testFlux() throws InterruptedException {
        doTest(false);
    }

    @Test
    public void testMono() throws InterruptedException {
        doTest(true);
    }

    public void doTest(boolean useMono) throws InterruptedException {
        AtomicBoolean bool = new AtomicBoolean(false);

        Observable<Integer> observable = Observable.just(1)
                .doOnCompleted(() -> bool.set(true))
                .doOnUnsubscribe(() -> System.out.println("Unsubscribed!"))
                .map(i -> i * 10);

        Publisher<Integer> publisher = RxReactiveStreams.toPublisher(observable);

        Mono<Integer> mono = useMono ? Mono.from(publisher) : Flux.from(publisher).singleOrEmpty();
        int res = mono
                .map(i -> i * 10)
                .block();

        assertEquals(100, res);
        assertTrue("OnCompleted handler has not been called", bool.get());
    }
}
