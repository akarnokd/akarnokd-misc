package hu.akarnokd.comparison;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.rxjava2.parallel.ParallelFlowable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ParallelPerf {

    @Param({"8"/*, "16", "32", "64", "128", "256"*/ })
    public int count;
    
    @Param({"1", "10", "100", "1000", "10000"})
    public int cost;
    
    @Benchmark
    public Object parallelStream() {
        return IntStream.range(0, count).parallel().map(v -> { Blackhole.consumeCPU(cost); return v; })
        .count();
    }
    
    @Benchmark
    public Object parallelFlowable() {
        return ParallelFlowable.from(Flowable.range(0, count)).runOn(Schedulers.computation())
        .map(v -> { Blackhole.consumeCPU(cost); return v; })
        .sequential()
        .blockingLast();
    }
}
