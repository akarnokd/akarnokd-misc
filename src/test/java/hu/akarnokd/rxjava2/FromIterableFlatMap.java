package hu.akarnokd.rxjava2;

import java.util.*;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;

public class FromIterableFlatMap {

    static class Result { }

    public static Observable<Result> getResults(List<Long> requests) {
        return Observable.fromIterable(requests)
                .flatMap( aLong -> {
                    Result[] items = {new Result(), new Result()};
                    return Observable.fromIterable(Arrays.asList(items));
                });
    }

    @Test
    public void test() {
        List<Long> ids = new ArrayList<>(Arrays.asList(1L, 2L, 3L));

        getResults(ids)
       .subscribe(new DisposableObserver<Result>() {
           @Override
           public void onNext(Result item) {
               System.out.println("onNext: " + item);
           }

           @Override
           public void onComplete() {
               System.out.println("onCompleted: ");
           }

           @Override
           public void onError(Throwable e) {
               System.out.println("onError: " + e.getMessage());
           }
       });
    }
}
