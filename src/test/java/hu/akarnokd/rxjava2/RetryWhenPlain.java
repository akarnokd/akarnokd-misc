package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.*;

public class RetryWhenPlain {

    public static <T> Observable<T> retryWhen(Observable<T> source, Function<Observable<Throwable>, Observable<?>> handler) {
        return Observable.<T>defer(() -> {
            PublishSubject<Throwable> errorSignal = PublishSubject.create();

            Observable<?> retrySignal = handler.apply(errorSignal);

            BehaviorSubject<Observable<T>> sources = BehaviorSubject.createDefault(source);

            Disposable d = retrySignal.map(v -> source)
                    .subscribe(sources::onNext, sources::onError, sources::onComplete);

            return sources.concatMap(src -> {
                return src
                        .doOnComplete(() -> errorSignal.onComplete())
                        .onErrorResumeNext(e -> {
                    errorSignal.onNext(e);
                    return Observable.<T>empty();
                });
            }).doFinally(() -> d.dispose());
        });
    }
    
    @Test
    public void test() {
        int[] count = { 3 };
        Observable.<String>defer(() -> {
            if (count[0]-- == 0) {
                return Observable.just("Success");
            }
            return Observable.error(new IOException());
        })
        .compose(o -> retryWhen(o, 
                f -> f.flatMap(e -> {
                    System.out.println("Retrying...");
                    return Observable.timer(1, TimeUnit.SECONDS);
                }))
        )
        .blockingSubscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
    }
}
