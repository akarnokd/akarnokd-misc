package hu.akarnokd.rxjava2;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.reactivex.subscribers.TestSubscriber;
import reactor.core.publisher.Flux;

public class UnicastSingleTest {

    public static void main(String[] args) throws Exception {
        /*
        Flowable.concat(
                Flowable.just("#").delay(20, TimeUnit.MILLISECONDS),
                Flowable.range(1, 10),
                Flowable.range(11, 5).compose(FlowableTransformers.spanout(15, TimeUnit.MILLISECONDS))
        )
                .window(10, TimeUnit.MILLISECONDS)
                .subscribe(flx -> flx.subscribe(System.out::println));

        Thread.sleep(5000);
        */

        Flux.<Object>concat(
                Flux.just("#").delayElements(Duration.ofMillis(20)),
                Flux.range(1, 10),
                Flux.range(11, 5).delayElements(Duration.ofMillis(15))
        )
        .subscribeWith(new TestSubscriber<>())
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult("#", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        Flux.concat(
                Flux.just("#").delayElements(Duration.ofMillis(20)),
                Flux.range(1, 10),
                Flux.range(11, 5).delayElements(Duration.ofMillis(15))
        )
                .windowTimeout(10, Duration.ofMillis(1))
                .subscribe(flx -> flx.subscribe(System.out::println));

        Thread.sleep(5000);
    }
}
