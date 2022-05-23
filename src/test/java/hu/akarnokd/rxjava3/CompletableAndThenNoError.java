package hu.akarnokd.rxjava3;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class CompletableAndThenNoError {

    public static void main(String[] args) {
        
        Completable.error(new Exception())
        .toObservable()
        .flatMap(
                v -> Observable.<Boolean>never(), 
                e -> Observable.just(false), 
                () -> Observable.just(true)
            )
        .subscribe(System.out::println);
    }
}
