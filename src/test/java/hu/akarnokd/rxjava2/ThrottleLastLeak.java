package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class ThrottleLastLeak {

    public static void main(String[] args) throws Exception {
        Disposable d = PublishSubject.create()
        .throttleLast(100, TimeUnit.MILLISECONDS)
        .subscribe(System.out::println);
        
        Thread.sleep(10000);
        
        d.dispose();
        
        Thread.sleep(1000000);
    }
}
