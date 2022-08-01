package hu.akarnokd.rxjava3;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;

public class ZipPattern {

    @Test
    public void test() {
Observable
.zip(Observable.just(-1L).concatWith(Observable.interval(10, 5, TimeUnit.SECONDS)),
     Observable.range(1, 5), 
     (aLong, integer1) -> {
          return new Pair<Long, Integer>(aLong, integer1);
     }
)
.concatMap(longIntegerPair -> {
       System.out.println("DATA " + longIntegerPair.getValue());
       return Observable.just(longIntegerPair.getValue())
               .delay(longIntegerPair.getKey() < 0 ? 10 : 5, TimeUnit.SECONDS)
               .flatMap(value -> {
                   System.out.println("[performing operation] " + value);
                   return Observable.just(value);
              });
})
.toList()
.toObservable()
.blockingSubscribe();
    }
}
