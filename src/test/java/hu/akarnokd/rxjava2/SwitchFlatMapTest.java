package hu.akarnokd.rxjava2;

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.functions.Func1;
import rx.observers.AssertableSubscriber;
import rx.subjects.*;

public class SwitchFlatMapTest {

    public static void main(String[] args) {
        PublishSubject<Integer> ps = PublishSubject.create();
        
        @SuppressWarnings("unchecked")
        PublishSubject<Integer>[] pss = new PublishSubject[3];
        for (int i = 0; i < pss.length; i++) {
            pss[i] = PublishSubject.create();
        }
        
        AssertableSubscriber<Integer> ts = ps
        .compose(switchFlatMap(2, v -> pss[v]))
        .test();
        
        ps.onNext(0);
        ps.onNext(1);
        
        pss[0].onNext(1);
        pss[0].onNext(2);
        pss[0].onNext(3);
        
        pss[1].onNext(10);
        pss[1].onNext(11);
        pss[1].onNext(12);
        
        ps.onNext(2);
        
        pss[0].onNext(4);
        
        pss[2].onNext(20);
        pss[2].onNext(21);
        pss[2].onNext(22);
        
        pss[1].onCompleted();
        pss[2].onCompleted();
        ps.onCompleted();
        
        ts.assertResult(1, 2, 3, 10, 11, 12, 20, 21, 22);
    }
    
    public static <T, R> Observable.Transformer<T, R> switchFlatMap(
            int n, Func1<T, Observable<R>> mapper) {
        return f -> 
            Observable.defer(() -> {
                final AtomicInteger ingress = new AtomicInteger();
                final Subject<Integer, Integer> cancel = 
                        PublishSubject.<Integer>create().toSerialized();
                return f.flatMap(v -> {
                    int id = ingress.getAndIncrement();
                    Observable<R> o = mapper.call(v)
                            .takeUntil(cancel.filter(e -> e == id + n));
                    cancel.onNext(id);
                    return o;
                });
            })
        ;
    }
}
