package hu.akarnokd.reactor;

import java.util.function.Consumer;

import hu.akarnokd.rxjava2.Observable;
import reactor.core.publisher.Flux;

public class StrangeFilter {

    public static void main(String[] args) {

        {
        Flux<Integer> nums = Flux.range(1, Integer.MAX_VALUE)
        .take(20)
        .cache(20)
        .doOnNext(print("nums"))
        ;


        Flux<Integer> filtered = nums
                .filter((i) -> i%5==0)
//                .doOnNext(print("filt"))
                .take(20)
                .cache(20)
                ;


        filtered.subscribe(print("end"));
        }
        
        System.out.println("---");
        
        {
        Observable<Integer> nums = Observable.range(1, Integer.MAX_VALUE)
        .take(20)
        .cache(20)
//        .doOnNext(print("nums"))
        ;


        Observable<Integer> filtered = nums
                .filter((i) -> i%5==0)
//                .doOnNext(print("filt"))
                .take(20)
                .cache(20)
                ;


        filtered.subscribe((e) -> System.out.println("end: "+e));
        }
    }

    private static <T> Consumer<T> print(String msg) {
        return (e) -> System.out.println(msg+": "+e);
    }

}