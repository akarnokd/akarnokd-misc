package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.*;

public class BehaviorMulticast {

    @Test
    public void test() {
        Subject<String> subject = PublishSubject.create();
        Observable<String> observable = subject.scan("zero", (a, b) -> a + ", " + b);
        Subject<String> multicast = BehaviorSubject.create();

        observable.subscribe(multicast);

        Disposable first = multicast.subscribe(System.out::println); // "zero"
        subject.onNext("one"); // "zero, one"
        first.dispose();
        Disposable second = multicast.subscribe(System.out::println); // "zero, one"
        subject.onNext("two"); // "zero, one, two"
        second.dispose();
    }
}
