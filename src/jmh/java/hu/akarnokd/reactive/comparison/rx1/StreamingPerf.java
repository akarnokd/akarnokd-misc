package hu.akarnokd.reactive.comparison.rx1;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfRxSubscriber;
import rx.Observable;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class StreamingPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    rx.Observable<Integer> rangeRxObservable;

    rx.Observable<Integer> arrayRxObservable;

    rx.Observable<Integer> iterableRxObservable;

    rx.Observable<Integer> concatMapJustRxObservable;

    rx.Observable<Integer> concatMapRangeRxObservable;

    rx.Observable<Integer> concatMapXRangeRxObservable;

    rx.Observable<Integer> flatMapJustRxObservable;

    rx.Observable<Integer> flatMapRangeRxObservable;

    rx.Observable<Integer> flatMapXRangeRxObservable;

    rx.Observable<Integer> flattenJustRxObservable;

    rx.Observable<Integer> flattenRangeRxObservable;

    rx.Observable<Integer> flattenXRangeRxObservable;

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

        Observable<Integer> just1x = Observable.just(1);
        Observable<Integer> range1x = Observable.range(1, 2);
        Observable<Integer> arrayX1x = Observable.from(arrayX);

        rangeRxObservable = Observable.range(1, count);

        arrayRxObservable = Observable.from(array);

        iterableRxObservable = Observable.from(Arrays.asList(array));

        concatMapJustRxObservable = arrayRxObservable.concatMap(v -> just1x);

        concatMapRangeRxObservable = arrayRxObservable.concatMap(v -> range1x);

        concatMapXRangeRxObservable = arrayRxObservable.concatMap(v -> arrayX1x);

        flatMapJustRxObservable = arrayRxObservable.flatMap(v -> just1x);

        flatMapRangeRxObservable = arrayRxObservable.flatMap(v -> range1x);

        flatMapXRangeRxObservable = arrayRxObservable.flatMap(v -> arrayX1x);


        flattenJustRxObservable = arrayRxObservable.flatMapIterable(v -> justIt);

        flattenRangeRxObservable = arrayRxObservable.flatMapIterable(v -> rangeIt);

        flattenXRangeRxObservable = arrayRxObservable.flatMapIterable(v -> arrayXIt);

        // --------------------------------------------------------------------------
    }

    @Benchmark
    public void rangeRxObservable(Blackhole bh) {
        rangeRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void arrayRxObservable(Blackhole bh) {
        arrayRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void iterableRxObservable(Blackhole bh) {
        iterableRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void concatMapJustRxObservable(Blackhole bh) {
        concatMapJustRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void concatMapRangeRxObservable(Blackhole bh) {
        concatMapRangeRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void concatMapXRangeRxObservable(Blackhole bh) {
        concatMapXRangeRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void flatMapJustRxObservable(Blackhole bh) {
        flatMapJustRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void flatMapRangeRxObservable(Blackhole bh) {
        flatMapRangeRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void flatMapXRangeRxObservable(Blackhole bh) {
        flatMapXRangeRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void flattenJustRxObservable(Blackhole bh) {
        flattenJustRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void flattenRangeRxObservable(Blackhole bh) {
        flattenRangeRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void flattenXRangeRxObservable(Blackhole bh) {
        flattenXRangeRxObservable.subscribe(new PerfRxSubscriber(bh));
    }

    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
}
