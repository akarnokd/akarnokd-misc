package hu.akarnokd.rxjava3;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observables.ConnectableObservable;

public class ShareExample {

    public static void main(String[] args) {
        ConnectableObservable<String> source = Observable.<String>create(emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("Foo");
            emitter.onNext("Bar");
            emitter.onNext("RxJava");
        }).publish();

        source.subscribe(e -> System.out.println("Observer 1: " + e));
        source.subscribe(e -> System.out.println("Observer 2: " + e));

        source.connect();
        
        Observable<String> source2 = Observable.<String>create(emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("Foo");
            emitter.onNext("Bar");
            emitter.onNext("RxJava");
        }).publish().refCount(2);

        source2.subscribe(e -> System.out.println("Observer 1: " + e));
        source2.subscribe(e -> System.out.println("Observer 2: " + e));
    }
}
