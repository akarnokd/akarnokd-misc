package hu.akarnokd.rxjava2;

import org.junit.Test;

import hu.akarnokd.rxjava2.debug.RxJavaAssemblyTracking;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class TrackedRefcount {

    @Test
    public void test() throws Exception {
        RxJavaAssemblyTracking.enable();
        
        System.out.println("start");

        PublishSubject<String> stringsEmitter = PublishSubject.create();

        Observable<String> combineSource = stringsEmitter
                .doOnSubscribe(d -> System.out.println("Connected!"))
                .replay(1)
                .refCount()
                .doOnSubscribe(d -> System.out.println("Subscribed!"))
                .doOnDispose(() -> System.out.println("Disposed!"));

        CompositeDisposable c = new CompositeDisposable();

        c.add(
                combineSource
                        //.subscribeOn(Schedulers.io())
                        .subscribe((string) -> System.out.println("A1:" + string))
        );
        c.add(
                combineSource
                        //.subscribeOn(Schedulers.io())
                        .subscribe((string) -> System.out.println("B1:" + string))
        );

        stringsEmitter.onNext("s1");

        Thread.sleep(100);
        c.clear();

        Thread.sleep(100);

        System.out.println("---");

        combineSource
            //.subscribeOn(Schedulers.io())
            //.subscribe((string) -> System.out.println("A2:" + string));
                .subscribe((string) -> System.out.println("A2:" + string), Throwable::printStackTrace, () -> System.out.println("Done A2"));

        combineSource
            //.subscribeOn(Schedulers.io())
                .subscribe((string) -> System.out.println("B2:" + string), Throwable::printStackTrace, () -> System.out.println("Done B2"));

        Thread.sleep(1000);

        stringsEmitter.onNext("s2");

        Thread.sleep(1000);

        stringsEmitter.onNext("s3");
        stringsEmitter.onNext("s4");
    }
}
