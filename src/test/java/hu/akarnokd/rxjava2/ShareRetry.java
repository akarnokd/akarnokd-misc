package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;

public class ShareRetry {
    @Test
    public void testIntervalMapShareRetry() throws InterruptedException {
        final Flowable<Long> flux = Flowable.interval(250, TimeUnit.MILLISECONDS).compose(report("interval"))
                .map(round -> {
                    if (round == 3) {
                        throw new RuntimeException();
                    }
                    return round;
                }).compose(report("map"))
                .share().compose(report("share"))
                .retry(3).compose(report("retry"));

        final Disposable disposable = flux.subscribe(System.out::println);

        Thread.sleep(5_000);
        System.out.println("Timeout; disposing all");
        disposable.dispose();
    }

    private static <T> FlowableTransformer<T, T> report(final String name) {
        return flux -> flux
                .doOnSubscribe(s -> System.out.println("onSubscribe " + name + " " + s))
                .doOnEach(s -> System.out.println(s))
                .doOnCancel(() -> System.out.println("onCancel " + name));
    }
}
