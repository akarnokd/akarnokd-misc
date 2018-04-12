package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;

public class PublishFuncExample {

    @Test
    public void test() throws Exception {
        Observable<Integer> source = Observable.range(1, 5)
                .delaySubscription(1, TimeUnit.SECONDS);

            Function<Observable<Integer>, Observable<Integer>> func = o ->
                Observable.merge(o.take(1), o.takeLast(1));

            Observable<Integer> forkAndJoin = Observable.defer(() -> {
                ConnectableObservable<Integer> conn = source
                    .doOnSubscribe(s -> System.out.println("Subscribed!"))
                    .publish();
                Observable<Integer> result = func.apply(conn);
                conn.connect();
                return result;
            });

            forkAndJoin.subscribe(System.out::println);
            forkAndJoin.subscribe(System.out::println);
            forkAndJoin.subscribe(System.out::println);
        
            Thread.sleep(10000);
    }
}
