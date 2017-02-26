package hu.akarnokd.rxjava;

import org.junit.Test;

import io.reactivex.disposables.Disposable;
import rx.*;
import rx.subjects.PublishSubject;

public class Replay1RetentionTest {

    @Test
    public void test() {
        
        PublishSubject<Integer> ps = PublishSubject.create();
        
        Observable<Integer> o = ps.replay(1).refCount();
        Subscription s = o.subscribe(System.out::println);
        
        ps.onNext(1);
        
        ps.onNext(2);
        
        ps.onNext(3);
        
        System.out.println(s);
        
        o.subscribe(System.out::println);
    }
    
    @Test
    public void test2() {
        
        io.reactivex.subjects.PublishSubject<Integer> ps = io.reactivex.subjects.PublishSubject.create();
        
        io.reactivex.Observable<Integer> o = ps.replay(1).refCount();
        Disposable s = o.subscribe(System.out::println);
        
        ps.onNext(1);
        
        ps.onNext(2);
        
        ps.onNext(3);
        
        System.out.println(s);
        
        o.subscribe(System.out::println);
    }
}
