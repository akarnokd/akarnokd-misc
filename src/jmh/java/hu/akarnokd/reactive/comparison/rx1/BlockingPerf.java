package hu.akarnokd.reactive.comparison.rx1;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class BlockingPerf {

    rx.Observable<Integer> firstRxObservable;

    rx.observables.BlockingObservable<Integer> firstRxBlockingObservable;

    rx.Observable<Integer> lastRxObservable;

    rx.observables.BlockingObservable<Integer> lastRxBlockingObservable;

    rx.Single<Integer> firstRxSingle;

    rx.singles.BlockingSingle<Integer> firstRxBlockingSingle;

    rx.Completable emptyRxCompletable;

    rx.Observable<Integer> emptyRxObservable;
    rx.observables.BlockingObservable<Integer> emptyRxBlockingObservable;

    @Setup
    public void setup() {

        firstRxObservable = rx.Observable.just(1);
        firstRxBlockingObservable = firstRxObservable.toBlocking();

        Integer[] array = new Integer[1_000_000];
        Arrays.fill(array, 777);

        lastRxObservable = rx.Observable.from(array);
        lastRxBlockingObservable = lastRxObservable.toBlocking();

        firstRxSingle = rx.Single.just(1);

        firstRxBlockingSingle = firstRxSingle.toBlocking();

        emptyRxCompletable = rx.Completable.complete();

        emptyRxObservable = rx.Observable.empty();
        emptyRxBlockingObservable = emptyRxObservable.toBlocking();
    }

    @Benchmark
    public Object firstRxObservable(Blackhole bh) {
        return firstRxObservable.toBlocking().first();
    }

    @Benchmark
    public Object firstRxBlockingObservable(Blackhole bh) {
        return firstRxBlockingObservable.first();
    }

    @Benchmark
    public Object lastRxObservable(Blackhole bh) {
        return lastRxObservable.toBlocking().last();
    }

    @Benchmark
    public Object lastRxBlockingObservable(Blackhole bh) {
        return lastRxBlockingObservable.last();
    }

    @Benchmark
    public Object firstRxSingle(Blackhole bh) {
        return firstRxSingle.toBlocking().value();
    }

    @Benchmark
    public Object firstRxBlockingSingle(Blackhole bh) {
        return firstRxBlockingSingle.value();
    }

    @Benchmark
    public Object emptyRxCompletable(Blackhole bh) {
        return emptyRxCompletable.get();
    }

    @Benchmark
    public Object emptyRxObservable(Blackhole bh) {
        return emptyRxBlockingObservable.lastOrDefault(null);
    }
}
