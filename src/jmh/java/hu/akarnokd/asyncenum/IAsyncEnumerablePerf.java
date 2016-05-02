package hu.akarnokd.asyncenum;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.comparison.LatchedRSObserver;
import rsc.publisher.Px;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh='IxPerf'
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class IAsyncEnumerablePerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    public int count;

    Ax<Integer> axRange;
    
    Px<Integer> pxRange;
    
    @Setup
    public void setup() {
        axRange = Ax.range(1, count);
        
        pxRange = Px.range(1, count);
    }
    
    @Benchmark
    public void range_ax(Blackhole bh) {
        PerfSyncConsumer psc = new PerfSyncConsumer(bh, axRange.enumerator());
        psc.consume();
    }
    
    @Benchmark
    public void range_px(Blackhole bh) {
        pxRange.subscribe(new LatchedRSObserver<>(bh));
    }
}
