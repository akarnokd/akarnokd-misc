package hu.akarnokd.rxjava2;

import io.reactivex.*;

public class WindowDisposed {

    static Observable<Integer> observable(int n) {
        return Observable.create(emitter -> {
            System.out.println("Subscribed: " + n);
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.setCancellable(() -> System.out.println("Cancelled " + n));
        });
    }
    
    static Flowable<Integer> flowable(int n) {
        return Flowable.create(emitter -> {
            System.out.println("Subscribed: " + n);
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.setCancellable(() -> System.out.println("Cancelled " + n));
        }, BackpressureStrategy.MISSING);
    }
    
    public static void main(String[] args) {
        observable(1)
        .window(observable(2).filter(v -> v == 1)
                .doOnDispose(() -> System.out.println("Dispose 2"))
                , w -> observable(3).filter(v -> v == 1)
                .doOnDispose(() -> System.out.println("Dispose 3")))
        .doOnNext(System.out::println)
        .subscribe()
        .dispose();
        
        System.out.println("---");
        
        flowable(1)
        .window(flowable(2).filter(v -> v == 1)
                .doOnCancel(() -> System.out.println("Dispose 2"))
                , w -> flowable(3).filter(v -> v == 1)
                .doOnCancel(() -> System.out.println("Dispose 3")))
        .doOnNext(System.out::println)
        .subscribe()
        .dispose();
    }
}
