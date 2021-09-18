package hu.akarnokd.rxjava2;

import org.junit.Test;

import hu.akarnokd.rxjava2.debug.RxJavaAssemblyTracking;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class RefCountTrackingTest {

    @Test
    public void test() throws Exception {
        RxJavaAssemblyTracking.enable();

        PublishSubject<Integer> ps = PublishSubject.create();

        Observable<Integer> source = ps.replay(1).refCount();

        Disposable d1 = source.subscribe(v -> System.out.println("A1: " + v));
        Disposable d2 = source.subscribe(v -> System.out.println("B1: " + v));

        ps.onNext(1);

        d1.dispose();
        d2.dispose();

        System.out.println(ps.hasObservers());

        source.subscribe(v -> System.out.println("A2: " + v));

        System.out.println(ps.hasObservers());

        source.subscribe(v -> System.out.println("B2: " + v));

        ps.onNext(2);
        ps.onNext(3);
        ps.onNext(4);
    }
}
