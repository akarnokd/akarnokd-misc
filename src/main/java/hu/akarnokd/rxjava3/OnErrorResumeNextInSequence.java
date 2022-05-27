package hu.akarnokd.rxjava3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.core.Observable;

public class OnErrorResumeNextInSequence {

    public static void main(String[] args) {
List<Observable<Integer>> sources = new ArrayList<>();

for (int i = 0; i < 10; i++) {
    final int j = i;
    if (i == 5) {
        sources.add(Observable.just(i)
                .doOnSubscribe(d -> System.out.println("Subscribed: " + j)));
    } else {
        sources.add(Observable.<Integer>error(new Exception("" + i))
                .doOnSubscribe(d -> System.out.println("Subscribed: " + j))
        );
    }
}

        Observable.defer(() -> {
            AtomicReference<Throwable> lastError = new AtomicReference<>();
            AtomicBoolean lastSuccessful = new AtomicBoolean();
            return Observable.fromIterable(sources)
                             .takeWhile(t -> !lastSuccessful.get())
                             .concatMap(source -> {
                                 return source
                                        .doOnComplete(() -> lastSuccessful.set(true))
                                        .doOnError(e -> lastError.set(e))
                                        .onErrorComplete();
                             })
                             .concatWith(Observable.defer(() -> {
                                 if (lastSuccessful.get()) {
                                     return Observable.empty();
                                 }
                                 return Observable.error(lastError.get());
                             }));

        })
        .test()
        .assertResult(5);
    }
}
