package hu.akarnokd.rxjava2;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;

public class ParallelProducing {
    public static void main(String[] args) {
AtomicInteger j = new AtomicInteger(0);
AtomicInteger i = new AtomicInteger(0);
Maybe<Integer> maybeSource = Maybe.create(emitter -> emitter.onSuccess(i.incrementAndGet()));
maybeSource
.repeat()
.parallel(30, 1)
.runOn(Schedulers.io(), 1)
.map(Object::toString)
.sequential(1)
.take(100)
.blockingSubscribe(v -> System.out.println(j.incrementAndGet() + " - " + v));

System.out.println("Consumed: " + i.get());
    }
}
