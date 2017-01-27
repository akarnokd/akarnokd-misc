package hu.akarnokd.comparison;

import java.util.concurrent.*;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.Flowable;
import io.reactivex.parallel.ParallelFlowable;
import io.reactivex.schedulers.Schedulers;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ParallelPerf {

    @Param({"1024" })
    public int count;

    @Param({"1", "10", "100", "1000", "10000"})
    public int cost;

    Flowable<Integer> flowable;

    Flowable<Integer> flowableFJ;

    @Setup
    public void setup() {
        flowable = ParallelFlowable.from(Flowable.range(0, count)).runOn(Schedulers.computation())
        .filter(v -> { Blackhole.consumeCPU(cost); return false; })
        .sequential();

        flowableFJ = ParallelFlowable.from(Flowable.range(0, count))
                .runOn(Schedulers.from(ForkJoinPool.commonPool()))
        .filter(v -> { Blackhole.consumeCPU(cost); return false; })
        .sequential();
    }

    @Benchmark
    public Object parallelStream() {
        return IntStream.range(0, count).parallel().filter(v -> { Blackhole.consumeCPU(cost); return false; })
        .findAny();
    }

    @Benchmark
    public Object parallelFlowable() {
        return flowable.blockingLast(0);
    }

    @Benchmark
    public Object parallelFlowableForkJoin() {
        return flowableFJ.blockingLast(0);
    }
}
