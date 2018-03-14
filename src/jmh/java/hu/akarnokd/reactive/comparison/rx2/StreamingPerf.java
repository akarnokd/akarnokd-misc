package hu.akarnokd.reactive.comparison.rx2;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfConsumer;
import io.reactivex.Flowable;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class StreamingPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    Flowable<Integer> rangeRx2Flowable;

    Flowable<Integer> arrayRx2Flowable;

    Flowable<Integer> iterableRx2Flowable;

    Flowable<Integer> concatMapJustRx2Flowable;

    Flowable<Integer> concatMapRangeRx2Flowable;

    Flowable<Integer> concatMapXRangeRx2Flowable;

    Flowable<Integer> flatMapJustRx2Flowable;

    Flowable<Integer> flatMapRangeRx2Flowable;

    Flowable<Integer> flatMapXRangeRx2Flowable;

    Flowable<Integer> flattenJustRx2Flowable;

    Flowable<Integer> flattenRangeRx2Flowable;

    Flowable<Integer> flattenXRangeRx2Flowable;

    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    io.reactivex.Observable<Integer> rangeRx2Observable;

    io.reactivex.Observable<Integer> arrayRx2Observable;

    io.reactivex.Observable<Integer> iterableRx2Observable;

    io.reactivex.Observable<Integer> concatMapJustRx2Observable;

    io.reactivex.Observable<Integer> concatMapRangeRx2Observable;

    io.reactivex.Observable<Integer> concatMapXRangeRx2Observable;

    io.reactivex.Observable<Integer> flatMapJustRx2Observable;

    io.reactivex.Observable<Integer> flatMapRangeRx2Observable;

    io.reactivex.Observable<Integer> flatMapXRangeRx2Observable;

    io.reactivex.Observable<Integer> flattenJustRx2Observable;

    io.reactivex.Observable<Integer> flattenRangeRx2Observable;

    io.reactivex.Observable<Integer> flattenXRangeRx2Observable;


    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    @Setup
    public void setup() {
        Integer[] array = new Integer[count];
        Arrays.fill(array, 777);

        Integer[] arrayX = new Integer[1_000_000 / count];
        Arrays.fill(arrayX, 777);

        Iterable<Integer> justIt = Collections.singletonList(1);
        Iterable<Integer> rangeIt = Arrays.asList(1, 2);
        Iterable<Integer> arrayXIt = Arrays.asList(arrayX);

        // --------------------------------------------------------------------------

        Flowable<Integer> just2x = Flowable.just(1);
        Flowable<Integer> range2x = Flowable.range(1, 2);
        Flowable<Integer> arrayX2x = Flowable.fromArray(arrayX);

        rangeRx2Flowable = Flowable.range(1, count);

        arrayRx2Flowable = Flowable.fromArray(array);

        iterableRx2Flowable = Flowable.fromIterable(Arrays.asList(array));

        concatMapJustRx2Flowable = arrayRx2Flowable.concatMap(v -> just2x);

        concatMapRangeRx2Flowable = arrayRx2Flowable.concatMap(v -> range2x);

        concatMapXRangeRx2Flowable = arrayRx2Flowable.concatMap(v -> arrayX2x);

        flatMapJustRx2Flowable = arrayRx2Flowable.flatMap(v -> just2x);

        flatMapRangeRx2Flowable = arrayRx2Flowable.flatMap(v -> range2x);

        flatMapXRangeRx2Flowable = arrayRx2Flowable.flatMap(v -> arrayX2x);


        flattenJustRx2Flowable = arrayRx2Flowable.flatMapIterable(v -> justIt);

        flattenRangeRx2Flowable = arrayRx2Flowable.flatMapIterable(v -> rangeIt);

        flattenXRangeRx2Flowable = arrayRx2Flowable.flatMapIterable(v -> arrayXIt);

        // --------------------------------------------------------------------------

        io.reactivex.Observable<Integer> just2xx = io.reactivex.Observable.just(1);
        io.reactivex.Observable<Integer> range2xx = io.reactivex.Observable.range(1, 2);
        io.reactivex.Observable<Integer> arrayX2xx = io.reactivex.Observable.fromArray(arrayX);

        rangeRx2Observable = io.reactivex.Observable.range(1, count);

        arrayRx2Observable = io.reactivex.Observable.fromArray(array);

        iterableRx2Observable = io.reactivex.Observable.fromIterable(Arrays.asList(array));

        concatMapJustRx2Observable = arrayRx2Observable.concatMap(v -> just2xx);

        concatMapRangeRx2Observable = arrayRx2Observable.concatMap(v -> range2xx);

        concatMapXRangeRx2Observable = arrayRx2Observable.concatMap(v -> arrayX2xx);

        flatMapJustRx2Observable = arrayRx2Observable.flatMap(v -> just2xx);

        flatMapRangeRx2Observable = arrayRx2Observable.flatMap(v -> range2xx);

        flatMapXRangeRx2Observable = arrayRx2Observable.flatMap(v -> arrayX2xx);


        flattenJustRx2Observable = arrayRx2Observable.flatMapIterable(v -> justIt);

        flattenRangeRx2Observable = arrayRx2Observable.flatMapIterable(v -> rangeIt);

        flattenXRangeRx2Observable = arrayRx2Observable.flatMapIterable(v -> arrayXIt);

        // --------------------------------------------------------------------------
    }

    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    @Benchmark
    public void rangeRx2Flowable(Blackhole bh) {
        rangeRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void arrayRx2Flowable(Blackhole bh) {
        arrayRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void iterableRx2Flowable(Blackhole bh) {
        iterableRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapJustRx2Flowable(Blackhole bh) {
        concatMapJustRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapRangeRx2Flowable(Blackhole bh) {
        concatMapRangeRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapXRangeRx2Flowable(Blackhole bh) {
        concatMapXRangeRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapJustRx2Flowable(Blackhole bh) {
        flatMapJustRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapRangeRx2Flowable(Blackhole bh) {
        flatMapRangeRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapXRangeRx2Flowable(Blackhole bh) {
        flatMapXRangeRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenJustRx2Flowable(Blackhole bh) {
        flattenJustRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenRangeRx2Flowable(Blackhole bh) {
        flattenRangeRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenXRangeRx2Flowable(Blackhole bh) {
        flattenXRangeRx2Flowable.subscribe(new PerfConsumer(bh));
    }

    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    @Benchmark
    public void rangeRx2Observable(Blackhole bh) {
        rangeRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void arrayRx2Observable(Blackhole bh) {
        arrayRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void iterableRx2Observable(Blackhole bh) {
        iterableRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapJustRx2Observable(Blackhole bh) {
        concatMapJustRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapRangeRx2Observable(Blackhole bh) {
        concatMapRangeRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapXRangeRx2Observable(Blackhole bh) {
        concatMapXRangeRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapJustRx2Observable(Blackhole bh) {
        flatMapJustRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapRangeRx2Observable(Blackhole bh) {
        flatMapRangeRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapXRangeRx2Observable(Blackhole bh) {
        flatMapXRangeRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenJustRx2Observable(Blackhole bh) {
        flattenJustRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenRangeRx2Observable(Blackhole bh) {
        flattenRangeRx2Observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenXRangeRx2Observable(Blackhole bh) {
        flattenXRangeRx2Observable.subscribe(new PerfConsumer(bh));
    }


    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
}
