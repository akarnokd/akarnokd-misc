package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

@SuppressWarnings({ "unchecked", "rawtypes"})
public class TakeUntil {
    @Test
    public void test() {
        PublishSubject publishSubject = PublishSubject.create();

        Disposable disposable = Single.timer(1000 * 2, TimeUnit.MILLISECONDS)
                .takeUntil(publishSubject.firstOrError())
                .subscribe(
                        data -> System.out.println("ted"),
                        throwable -> System.err.println("ted" + throwable.toString())
                );
        disposable.dispose();

        publishSubject.onNext(1); // emit end of lifecycle
    }
}
