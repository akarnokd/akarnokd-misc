package hu.akarnokd.reactive.comparison.reactor;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfConsumer;
import reactor.core.publisher.Flux;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class StreamingPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    Flux<Integer> rangeReactorFlux;

    Flux<Integer> arrayReactorFlux;

    Flux<Integer> iterableReactorFlux;

    Flux<Integer> concatMapJustReactorFlux;

    Flux<Integer> concatMapRangeReactorFlux;

    Flux<Integer> concatMapXRangeReactorFlux;

    Flux<Integer> flatMapJustReactorFlux;

    Flux<Integer> flatMapRangeReactorFlux;

    Flux<Integer> flatMapXRangeReactorFlux;

    Flux<Integer> flattenJustReactorFlux;

    Flux<Integer> flattenRangeReactorFlux;

    Flux<Integer> flattenXRangeReactorFlux;

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

        Flux<Integer> justFx = Flux.just(1);
        Flux<Integer> rangeFx = Flux.range(1, 2);
        Flux<Integer> arrayXFx = Flux.fromArray(arrayX);

        rangeReactorFlux = Flux.range(1, count);

        arrayReactorFlux = Flux.fromArray(array);

        iterableReactorFlux = Flux.fromIterable(Arrays.asList(array));

        concatMapJustReactorFlux = arrayReactorFlux.concatMap(v -> justFx);

        concatMapRangeReactorFlux = arrayReactorFlux.concatMap(v -> rangeFx);

        concatMapXRangeReactorFlux = arrayReactorFlux.concatMap(v -> arrayXFx);

        flatMapJustReactorFlux = arrayReactorFlux.flatMap(v -> justFx);

        flatMapRangeReactorFlux = arrayReactorFlux.flatMap(v -> rangeFx);

        flatMapXRangeReactorFlux = arrayReactorFlux.flatMap(v -> arrayXFx);


        flattenJustReactorFlux = arrayReactorFlux.flatMapIterable(v -> justIt);

        flattenRangeReactorFlux = arrayReactorFlux.flatMapIterable(v -> rangeIt);

        flattenXRangeReactorFlux = arrayReactorFlux.flatMapIterable(v -> arrayXIt);
    }

    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    @Benchmark
    public void rangeReactorFlux(Blackhole bh) {
        rangeReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void arrayReactorFlux(Blackhole bh) {
        arrayReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void iterableReactorFlux(Blackhole bh) {
        iterableReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapJustReactorFlux(Blackhole bh) {
        concatMapJustReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapRangeReactorFlux(Blackhole bh) {
        concatMapRangeReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void concatMapXRangeReactorFlux(Blackhole bh) {
        concatMapXRangeReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapJustReactorFlux(Blackhole bh) {
        flatMapJustReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapRangeReactorFlux(Blackhole bh) {
        flatMapRangeReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flatMapXRangeReactorFlux(Blackhole bh) {
        flatMapXRangeReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenJustReactorFlux(Blackhole bh) {
        flattenJustReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenRangeReactorFlux(Blackhole bh) {
        flattenRangeReactorFlux.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void flattenXRangeReactorFlux(Blackhole bh) {
        flattenXRangeReactorFlux.subscribe(new PerfConsumer(bh));
    }

}
