package hu.akarnokd.rxjava;

import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import rx.Observable;

public final class StackOverflowDepth {

    private StackOverflowDepth() { }

    static void rxjava() {
        for (int i = 1; i < 100000; i += 1) {
            Observable<Integer> source = Observable.just(1);
            for (int j = 1; j <= i; j++) {
                source = source.doOnNext(e -> { System.out.print(""); });
            }

            try {
                source.test();
            } catch (StackOverflowError ex) {
                System.out.println(i);
                break;
            }
        }
    }

    static void rxjava2() {
        for (int i = 1; i < 100000; i += 1) {
            Flowable<Integer> source = Flowable.just(1);
            for (int j = 1; j <= i; j++) {
                source = source.doOnNext(e -> { System.out.print(""); });
            }

            try {
                source.test();
            } catch (StackOverflowError ex) {
                System.out.println(i);
                ex.printStackTrace();
                break;
            }
        }
    }

    static void reactor3() {
        for (int i = 1; i < 100000; i += 1) {
            Flux<Integer> source = Flux.just(1);
            for (int j = 1; j <= i; j++) {
                source = source.doOnNext(e -> { System.out.print(""); });
            }

            try {
                source.subscribe();
            } catch (StackOverflowError ex) {
                System.out.println(i);
                ex.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("RxJava");
        rxjava();
        System.out.println("RxJava 2");
        rxjava2();
        System.out.println("Reactor 3");
        reactor3();
    }
}
