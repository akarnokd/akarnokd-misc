package hu.akarnokd.comparison.classic;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import hu.akarnokd.rxjava2.math.*;
import ix.Ix;
import rx.Observable;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ForEachPerf {

    List<Integer> list;
    
    @Setup
    public void setup() {
        list = Ix.range(1, 1000).toList();
    }

    @Benchmark
    public long forLoop() {
        long total = 0;
        
        for (Integer i : list) {
            total += i;
        }
        return total;
    }
    
    @Benchmark
    public Object rx1() {
        return Observable.from(list).reduce(0, (a, b) -> a + b).toBlocking().single();
    }
    
    @Benchmark
    public Object rx2Obs() {
        return io.reactivex.Observable.fromIterable(list).reduce(0, (a, b) -> a + b).blockingGet();
    }
    
    @Benchmark
    public Object rx2ObsMath() {
        return MathObservable.sumInt(io.reactivex.Observable.fromIterable(list)).blockingSingle();
    }

    @Benchmark
    public Object rx2Flow() {
        return io.reactivex.Flowable.fromIterable(list).reduce(0, (a, b) -> a + b).blockingGet();
    }

    @Benchmark
    public Object rx2FlowMath() {
        return MathFlowable.sumInt(io.reactivex.Flowable.fromIterable(list)).blockingSingle();
    }
}