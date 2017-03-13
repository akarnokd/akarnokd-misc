package hu.akarnokd.rxjava2;

import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ExceptionAfterDispose {
    @Test
    public void test() throws Exception {
        final Disposable disposable = Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                long endTime = System.currentTimeMillis() + 2000;
                while (System.currentTimeMillis() < endTime) {
                }
                //network spends 2000ms, then throw an exception
                throw new IllegalStateException("error on network");
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
        //dispose it after 500ms
        Observable.timer(500, TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                disposable.dispose();
            }
        });

        Thread.sleep(3000);
    }
}