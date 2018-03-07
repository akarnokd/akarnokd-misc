package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class CrossTypeBackpressure {

    @Test
    public void test() {
        Completable completable =
                Observable.range(1, 1_000_000)
                          .flatMap((Integer integer) -> {
                              return Observable.just(integer)
                                            .observeOn(Schedulers.computation());
                              }
                          )
                          .toFlowable(BackpressureStrategy.BUFFER)
                          .doOnRequest(q -> System.out.println("qqq " + q))
                          .flatMapCompletable((Integer integer) ->
                                  Completable.defer(() -> {
                                      if (integer % 1000 == 0)
                                      {
                                          System.out.println("@@@ " + integer);
                                      }
                                      return Completable.timer(1, TimeUnit.MILLISECONDS);
                                  }), false, 1000)
                          ;

        completable.blockingAwait();
    }
}
