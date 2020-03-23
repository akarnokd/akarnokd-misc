package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LazyUndeliverable {

    private String simulateHeavyWork() throws InterruptedException {
        Thread.sleep(2000);
        return "Done";
    }
    
    @Test
    public void deferObservable() throws Exception {
        Disposable disposable = Observable.defer(() -> Observable.just(simulateHeavyWork()))
                .subscribeOn(Schedulers.io())
                .subscribe(System.out::println, throwable -> System.out.println(throwable.getMessage()));
       Thread.sleep(1000);
       disposable.dispose();

       Thread.sleep(5000);
    }
    
    @Test
    public void deferFlowable() throws Exception {
        Disposable disposable = Flowable.defer(() -> Flowable.just(simulateHeavyWork()))
                .subscribeOn(Schedulers.io())
                .subscribe(System.out::println, throwable -> System.out.println(throwable.getMessage()));
       Thread.sleep(1000);
       disposable.dispose();

       Thread.sleep(5000);
    }
}
