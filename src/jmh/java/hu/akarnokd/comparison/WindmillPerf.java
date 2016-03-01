package hu.akarnokd.comparison;

import java.util.concurrent.*;
import java.util.function.Consumer;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import hu.akarnokd.rxjava2.*;
import hu.akarnokd.rxjava2.internal.schedulers.SingleScheduler;
import hu.akarnokd.rxjava2.schedulers.Schedulers;
import io.windmill.core.*;
import reactivestreams.commons.publisher.PublisherBase;
import reactor.core.publisher.SchedulerGroup;
import reactor.core.util.WaitStrategy;
import reactor.rx.Fluxion;

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

    Publisher<Integer> rx2;

    Publisher<Integer> rx2Windmill;

    rx.Observable<Integer> rx1;

    rx.Observable<Integer> rx1Windmill;

    private ExecutorService exec1;

    private ExecutorService exec2;

    private CPUSet cs;
    
    Fluxion<Integer> flx;

    private SchedulerGroup sg1;

    private SchedulerGroup sg2;
    
    @Setup
    public void setup() {
        cs = CPUSet.builder().addSocket(0, 1).build();
        
        cs.start();
        
        cpu1 = cs.get(0);
        cpu2 = cs.get(1);
        
        exec1 = Executors.newSingleThreadExecutor();
        exec2 = Executors.newSingleThreadExecutor();
        
        rx2 = Observable.range(1, count).subscribeOn(new SingleScheduler()).observeOn(Schedulers.single());
        
        Scheduler s1 = Schedulers.from(r -> cpu1.schedule(r::run));
        Scheduler s2 = Schedulers.from(r -> cpu1.schedule(r::run));
        
        rx2Windmill = Observable.range(1, count).subscribeOn(s1).observeOn(s2);

        rx1 = rx.Observable.range(1, count).subscribeOn(rx.schedulers.Schedulers.computation()).observeOn(rx.schedulers.Schedulers.computation());
        
        rx.Scheduler s3 = rx.schedulers.Schedulers.from(r -> cpu1.schedule(r::run));
        rx.Scheduler s4 = rx.schedulers.Schedulers.from(r -> cpu2.schedule(r::run));
        
        rx1Windmill = rx.Observable.range(1, count).subscribeOn(s3).observeOn(s4);

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
        
        sg1 = SchedulerGroup.single("A", 1024, WaitStrategy.busySpin());
        sg2 = SchedulerGroup.single("B", 1024, WaitStrategy.busySpin());
        
        flx = Fluxion.range(1, count).publishOn(sg1).dispatchOn(sg2);
    }
    
    @TearDown
    public void teardown() {
        exec1.shutdown();
        exec2.shutdown();
        cs.halt();
        sg1.shutdown();
        sg2.shutdown();
    }

    static void await(int count, CountDownLatch latch) throws Exception {
        if (count < 1000) {
            while (latch.getCount() != 0) ;
        } else {
            latch.await();
        }
    }
    

    @Benchmark
    public void rsc(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        rsc.subscribe(o);
        await(count, o.latch);
    }

    @Benchmark
    public void fluxion(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        flx.subscribe(o);
        await(count, o.latch);
    }

    @Benchmark
    public void rscWindmill(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        rscWindmill.subscribe(o);
        
        await(count, o.latch);
    }

    @Benchmark
    public void rx2(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        rx2.subscribe(o);
        
        await(count, o.latch);
    }

    @Benchmark
    public void rx2Windmill(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        rx2Windmill.subscribe(o);
        
        await(count, o.latch);
    }

    @Benchmark
    public void rx1(Blackhole bh) throws Exception {
        LatchedObserver<Integer> o = new LatchedObserver<>(bh);
        rx1.subscribe(o);
        
        await(count, o.latch);
    }

    @Benchmark
    public void rx1Windmill(Blackhole bh) throws Exception {
        LatchedObserver<Integer> o = new LatchedObserver<>(bh);
        rx1Windmill.subscribe(o);
        
        await(count, o.latch);
    }

    @Benchmark
    public void executor(Blackhole bh) throws Exception {
        
        CountDownLatch cdl = new CountDownLatch(1);
        
        int c = count;
        for (int i = 0; i < c; i++) {
            int j = i;
            exec1.submit(() -> {
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }
        
        await(c, cdl);
    }

    @Benchmark
    public void forkjoin(Blackhole bh) throws Exception {
        
        CountDownLatch cdl = new CountDownLatch(1);
        
        ForkJoinPool fj = ForkJoinPool.commonPool();
        
        int c = count;
        for (int i = 0; i < c; i++) {
            int j = i;
            fj.submit(() -> {
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }
        
        await(c, cdl);
    }

    
    @Benchmark
    public void windmill(Blackhole bh) throws Exception {
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
    
    @Benchmark
    public void reactor(Blackhole bh) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);

        int c = count;
        for (int i = 0; i < c; i++) {
            int j = i;
            sg1.accept(() -> {
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }

        await(c, cdl);
    }
}
