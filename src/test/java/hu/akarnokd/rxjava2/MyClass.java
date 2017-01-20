package hu.akarnokd.rxjava2;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MyClass {

    public static void main(String[] args) throws Exception {

        Disposable d = Observable.just(1)
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<Integer, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(Integer integer) throws Exception {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            // ignored
                        }
                        return Observable.error(new IllegalStateException("example"));
                    }
                })
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onComplete() {}

                    @Override
                    public void onNext(Integer integer) {}
                });


        try {
            Thread.sleep(50);
            d.dispose();
        } catch (InterruptedException e) {
        }

        Thread.sleep(1000);
    }
}