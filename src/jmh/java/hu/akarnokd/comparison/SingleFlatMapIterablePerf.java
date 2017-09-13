package hu.akarnokd.comparison;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.PerfConsumer;
import io.reactivex.*;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh="EsetlegFlatMapIterablePerf"
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SingleFlatMapIterablePerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    public int count;

    Flowable<Integer> range;

    Flowable<Integer> seq;

    Flowable<Integer> seqDirect;

    Flowable<Integer> seqArray;

    @Setup
    public void setup() {
        Integer[] array = new Integer[count];
        Arrays.fill(array, 777);
        List<Integer> list = Arrays.asList(array);
        seq = Single.just(1).flattenAsFlowable(v -> list);
        seqDirect = Flowable.fromIterable(list);
        seqArray = Flowable.fromArray(array);
        range = Flowable.range(1, count);
    }

    @Benchmark
    public void range(Blackhole bh) {
        range.subscribe(new PerfConsumer(bh));
    }

//    @Benchmark
    public void seqIndirect(Blackhole bh) {
        seq.subscribe(new PerfConsumer(bh));
    }

//    @Benchmark
    public void seqDirect(Blackhole bh) {
        seqDirect.subscribe(new PerfConsumer(bh));
    }

//    @Benchmark
    public void seqArray(Blackhole bh) {
        seqArray.subscribe(new PerfConsumer(bh));
    }
}