package hu.akarnokd.reactive.comparison.rx2;

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

    io.reactivex.Observable<Integer> firstRx2Observable;

    io.reactivex.Observable<Integer> lastRx2Observable;

    io.reactivex.Flowable<Integer> firstRx2Flowable;

    io.reactivex.Flowable<Integer> lastRx2Flowable;

    io.reactivex.Single<Integer> firstRx2Single;

    io.reactivex.Completable emptyRx2Completable;

    io.reactivex.Maybe<Integer> firstRx2Maybe;

    io.reactivex.Maybe<Integer> emptyRx2Maybe;

    io.reactivex.Observable<Integer> emptyRx2Observable;

    io.reactivex.Flowable<Integer> emptyRx2Flowable;

    @Setup
    public void setup() {

        Integer[] array = new Integer[1_000_000];
        Arrays.fill(array, 777);

        firstRx2Observable = io.reactivex.Observable.just(1);

        lastRx2Observable = io.reactivex.Observable.fromArray(array);

        firstRx2Flowable = io.reactivex.Flowable.just(1);

        lastRx2Flowable = io.reactivex.Flowable.fromArray(array);

        firstRx2Single = io.reactivex.Single.just(1);

        emptyRx2Completable = io.reactivex.Completable.complete();

        firstRx2Maybe = io.reactivex.Maybe.just(1);

        emptyRx2Maybe = io.reactivex.Maybe.empty();

        emptyRx2Flowable = io.reactivex.Flowable.empty();

        emptyRx2Observable = io.reactivex.Observable.empty();
    }

    @Benchmark
    public Object firstRx2Observable(Blackhole bh) {
        return firstRx2Observable.blockingFirst();
    }

    @Benchmark
    public Object lastRx2Observable(Blackhole bh) {
        return lastRx2Observable.blockingLast();
    }

    @Benchmark
    public Object firstRx2Flowable(Blackhole bh) {
        return firstRx2Flowable.blockingFirst();
    }

    @Benchmark
    public Object lastRx2Flowable(Blackhole bh) {
        return lastRx2Flowable.blockingLast();
    }

    @Benchmark
    public Object firstRx2Single(Blackhole bh) {
        return firstRx2Single.blockingGet();
    }

    @Benchmark
    public Object emptyRx2Completable(Blackhole bh) {
        return emptyRx2Completable.blockingGet();
    }

    @Benchmark
    public Object firstRx2Maybe(Blackhole bh) {
        return firstRx2Maybe.blockingGet();
    }

    @Benchmark
    public Object emptyRx2Maybe(Blackhole bh) {
        return emptyRx2Maybe.blockingGet();
    }

    @Benchmark
    public Object emptyRx2Flowable(Blackhole bh) {
        return emptyRx2Flowable.blockingLast(null);
    }

    @Benchmark
    public Object emptyRx2Observable(Blackhole bh) {
        return emptyRx2Observable.blockingLast(null);
    }
}
