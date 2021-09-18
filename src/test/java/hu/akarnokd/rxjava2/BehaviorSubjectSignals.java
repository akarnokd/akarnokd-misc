package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.disposables.*;
import io.reactivex.subjects.BehaviorSubject;

public class BehaviorSubjectSignals {

    @Test
    public void test() throws Exception {

        CompositeDisposable cd = new CompositeDisposable();

        BehaviorSubject<Integer> bs = BehaviorSubject.create();

        startListening(cd, bs);

        bs.onNext(1);
        bs.onNext(2);

        System.out.println("----");

        startListening(cd, bs);
        bs.onNext(3);
    }

    void startListening(CompositeDisposable cd, BehaviorSubject<Integer> bs) {
        Disposable d = bs
        .doOnNext(v -> System.out.println("After subject " + v))
        .skip(1)
        .doOnNext(v -> System.out.println("After skip " + v))
        .take(1)
        .doOnNext(v -> System.out.println("After take " + v))
        .doOnDispose(() -> System.out.println("doOnDispose"))
        .subscribe(v -> {
            System.out.println("Observer " + v);
            stopListening(cd);
        })
        ;
        System.out.println("Before Composite.add");
        cd.add(d);
    }

    void stopListening(CompositeDisposable cd) {
        System.out.println("Stop listening");
        cd.clear();
    }
}
