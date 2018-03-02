package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;

public class IsDisposedTest {

    @Test
    public void test1() {
        Observable.just(1).subscribe(new Observer<Integer>() {

            Disposable disposable;

            @Override
            public void onSubscribe(Disposable disposable) {
                System.out.println("Subscribed");
                this.disposable = disposable;
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(integer);
                System.out.println(disposable.isDisposed());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error");
                System.out.println(disposable.isDisposed());
            }

            @Override
            public void onComplete() {
                System.out.println("Complete");
                System.out.println(disposable.isDisposed());
            }
        });
    }
    
    @Test
    public void test2() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> observableEmitter) throws Exception {
                if (!observableEmitter.isDisposed())
                    observableEmitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {

            Disposable disposable;

            @Override
            public void onSubscribe(Disposable disposable) {
                System.out.println("Subscribed");
                this.disposable = disposable;
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(integer);
                System.out.println(disposable.isDisposed());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error");
                System.out.println(disposable.isDisposed());
            }

            @Override
            public void onComplete() {
                System.out.println("Complete");
                System.out.println(disposable.isDisposed());
            }
        }); 
    }
}
