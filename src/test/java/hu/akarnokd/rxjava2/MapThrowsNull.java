package hu.akarnokd.rxjava2;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;

public class MapThrowsNull {

    public static void main(String[] args) {
        Observable.just("some name")
        .map(name -> mightThrowException(name))
        .subscribe(new Observer<Observable<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("onSubscribe: subscribed");
            }

            @Override
            public void onNext(Observable<String> value) {
                System.out.println("onNext: on next");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError: error");
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete: complete");
            }
        });
    }
    
    private static Observable<String> mightThrowException(String name) {
        throw new NullPointerException();
    }
}
