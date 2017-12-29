package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.*;
import io.reactivex.schedulers.Schedulers;

public class RetryWhenTest {

    @Test
    public void test() throws Exception {
        final Disposable[] disposable = new Disposable[1];
        Observable.just(0)
                .map(new Function<Object, Object>() {
                    @Override
                    public Object apply(@NonNull Object o) throws Exception {
                        System.out.println( "apply: before thread change: " + disposable[0].isDisposed());
                        return o;
                    }
                })
                .observeOn(Schedulers.io())           // If change thread, the next `map` will not be reachable
                .map(new Function<Object, Object>() {
                    @Override
                    public Object apply(@NonNull Object o) throws Exception {
                        System.out.println( "apply: after thread change: " + disposable[0].isDisposed());
                        return o;
                    }
                })
                .map(new Function<Object, Object>() {
                    @Override
                    public Object apply(@NonNull Object o) throws Exception {
                        throw new RuntimeException();
                    }
                })
    .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
        @Override
        public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable) throws Exception {
            return throwableObservable
            .takeWhile(new Predicate<Throwable>() {
                int counter;
                @Override
                public boolean test(Throwable v) throws Exception {
                    return ++counter < 2;
                }
            })
            .flatMap(new Function<Object, ObservableSource<?>>() {
                @Override
                public ObservableSource<?> apply(@NonNull Object o) throws Exception {
                    return Observable.timer(0, TimeUnit.MILLISECONDS);
                }
            });
        }
    })
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        System.out.println( "onSubscribe: ");
                        disposable[0] = d;
                    }

                    @Override
                    public void onNext(Object o) {
                        System.out.println( "onNext: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println( "onError: " + e);
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println( "onComplete: ");
                    }
                });
        
        Thread.sleep(1000);
    }
}
