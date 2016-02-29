package hu.akarnokd.comparison;

import java.util.concurrent.*;
import java.util.function.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import io.windmill.core.*;
import reactivestreams.commons.publisher.PublisherBase;

/**
 * Benchmark the Windmill library.
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class WindmillPerf {
    CPU cpu1;
    
    CPU cpu2;
    
    @Param({"1", "1000", "1000000"})
    public int count;
    
    Publisher<Integer> rsc;

    Publisher<Integer> rscWindmill;

    private ExecutorService exec1;

    private ExecutorService exec2;

    private CPUSet cs;
    
    @Setup
    public void setup() {
        cs = CPUSet.builder().addSocket(0, 1).build();
        
        cs.start();
        
        cpu1 = cs.get(0);
        cpu2 = cs.get(1);
        
        exec1 = Executors.newSingleThreadExecutor();
        exec2 = Executors.newSingleThreadExecutor();
        
        rsc = PublisherBase.range(1, count).subscribeOn(exec1).observeOn(exec2);
        
        Callable<Consumer<Runnable>> scheduler1 = () -> r -> {
            if (r != null) {
                cpu1.schedule(r::run);
            }
        };

        Callable<Consumer<Runnable>> scheduler2 = () -> r -> {
            if (r != null) {
                cpu2.schedule(r::run);
            }
        };
        
        rscWindmill = PublisherBase.range(1, count).subscribeOn(scheduler1).observeOn(scheduler2);
    }
    
    @TearDown
    public void teardown() {
        exec1.shutdown();
        exec2.shutdown();
        cs.halt();
    }
    
    @Benchmark
    public void rsc(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        rsc.subscribe(o);
        await(count, o.latch);
    }

    static void await(int count, CountDownLatch latch) throws Exception {
        if (count < 1000) {
            while (latch.getCount() != 0) ;
        } else {
            latch.await();
        }
    }
    
    @Benchmark
    public void rscWindmill(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        rscWindmill.subscribe(o);
        
        await(count, o.latch);
    }

    
    @Benchmark
    public void windmill(Blackhole bh) throws Exception {
        if (count >= 1_000_000) {
            throw new RuntimeException("Slow!");
        }
        
        CountDownLatch cdl = new CountDownLatch(1);
        
        int c = count;
        for (int i = 0; i < c; i++) {
            int j = i;
            cpu1.schedule(() -> {
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }
        
        await(c, cdl);
    }
}
