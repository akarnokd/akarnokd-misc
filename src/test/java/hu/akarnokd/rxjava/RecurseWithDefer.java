package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.Observable;

public class RecurseWithDefer {

    Observable<Integer> numbers(int start) {
        return Observable.just(start)
               .concatWith(Observable.defer(() -> numbers(start + 1)));
    }
    @Test
    public void test() {
        numbers(1)
        .take(1000)
        .subscribe(System.out::println, Throwable::printStackTrace);
    }

    io.reactivex.Observable<Integer> numbers2(int start) {
        return io.reactivex.Observable.just(start)
               .concatWith(io.reactivex.Observable.defer(() -> numbers2(start + 1)));
    }
    @Test
    public void test2() {
        numbers2(1)
        .take(1000)
        .subscribe(System.out::println, Throwable::printStackTrace);
    }
}
