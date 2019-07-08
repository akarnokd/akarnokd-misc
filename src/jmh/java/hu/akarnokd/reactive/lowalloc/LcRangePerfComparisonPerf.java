package hu.akarnokd.reactive.lowalloc;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.rxjava2.PerfConsumer;
import io.reactivex.Observable;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class LcRangePerfComparisonPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    public int count;

    Observable<Integer> observable;

    LcObservable<Integer> lcObservable;

    @Setup
    public void setup() {
        observable = createObservable();
        lcObservable = createLcObservable();
    }

    Observable<Integer> createObservable() {
        return Observable.range(1, count).map(v -> v + 1);
    }

    LcObservable<Integer> createLcObservable() {
        return LcObservable.range(1, count).map(v -> v + 1);
    }

//    @Benchmark
    public void freshObservable(Blackhole bh) {
        createObservable().subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void freshLcObservable(Blackhole bh) {
        createLcObservable().subscribe(new PerfConsumer(bh));
    }

//    @Benchmark
    public void cachedObservable(Blackhole bh) {
        observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void cachedLcObservable(Blackhole bh) {
        lcObservable.subscribe(new PerfConsumer(bh));
    }
}
