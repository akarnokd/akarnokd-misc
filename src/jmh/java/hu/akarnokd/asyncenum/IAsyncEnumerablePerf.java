package hu.akarnokd.asyncenum;

import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.comparison.LatchedRSObserver;
import rsc.publisher.Px;
import rsc.scheduler.ExecutorServiceScheduler;

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
    
    Ax<Integer> axAsync;

    Ax<Integer> axPipeline;
    
    Px<Integer> pxRange;
    
    Px<Integer> pxPipeline;

    Px<Integer> pxAsync;

    Px<Integer> pxAsyncClassic;

    ExecutorService exec1;
    
    ExecutorService exec2;
    
    @Setup
    public void setup() {
        axRange = Ax.range(1, count);
        pxRange = Px.range(1, count);
        
        exec1 = Executors.newSingleThreadExecutor();
        
        exec2 = Executors.newSingleThreadExecutor();

        axAsync = axRange.observeOn(exec1);
        
        axPipeline = axRange.subscribeOn(exec1).observeOn(exec2);
        
        ExecutorServiceScheduler s1 = new ExecutorServiceScheduler(exec1, false);
        ExecutorServiceScheduler s2 = new ExecutorServiceScheduler(exec2, false);

        pxAsync = pxRange.observeOn(exec1);

        pxAsyncClassic = pxRange.hide().observeOn(exec1);

        pxPipeline = pxRange.subscribeOn(s1).observeOn(s2);
    }
    
    @TearDown
    public void teardown() {
        
        exec1.shutdown();
        exec2.shutdown();
    }
    
    @Benchmark
    public void range_ax(Blackhole bh) {
        PerfSyncConsumer psc = new PerfSyncConsumer(bh, axRange.enumerator());
        psc.consume();
    }

    @Benchmark
    public void rangePipeline_ax(Blackhole bh) {
        PerfAsyncConsumer psc = new PerfAsyncConsumer(bh, axPipeline.enumerator());
        psc.consume(count);
    }

    @Benchmark
    public void rangeAsync_ax(Blackhole bh) {
        PerfAsyncConsumer psc = new PerfAsyncConsumer(bh, axAsync.enumerator());
        psc.consume(count);
    }

    @Benchmark
    public void range_px(Blackhole bh) {
        pxRange.subscribe(new LatchedRSObserver<>(bh));
    }
    
    @Benchmark
    public void rangePipeline_px(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> s = new LatchedRSObserver<>(bh);
        
        pxPipeline.subscribe(s);
        
        if (count <= 1000) {
            while (s.latch.getCount() != 0) ;
        } else {
            s.latch.await();
        }
    }

    @Benchmark
    public void rangeAsync_px(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> s = new LatchedRSObserver<>(bh);
        
        pxAsync.subscribe(s);
        
        if (count <= 1000) {
            while (s.latch.getCount() != 0) ;
        } else {
            s.latch.await();
        }
    }

    @Benchmark
    public void rangeAsyncClassic_px(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> s = new LatchedRSObserver<>(bh);
        
        pxAsyncClassic.subscribe(s);
        
        if (count <= 1000) {
            while (s.latch.getCount() != 0) ;
        } else {
            s.latch.await();
        }
    }

}
