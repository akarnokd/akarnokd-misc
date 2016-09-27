package hu.akarnokd.comparison.classic;

import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ClassicAsyncPerf {
    @Param({ "1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    ExecutorService exec;

    rx.subjects.PublishSubject<Integer> ps;
    
    PublishProcessor<Integer> pp;

    rx.Observable<Integer> observableExec;
    
    rx.Observable<Integer> observableFJ;
    
    Flowable<Integer> flowableExec;
    
    Flowable<Integer> flowableFJ;
    
    @Setup
    public void setup() throws Exception {
        exec = Executors.newSingleThreadExecutor();
        
        rx.Scheduler s1 = rx.schedulers.Schedulers.from(exec);
        io.reactivex.Scheduler s2 = io.reactivex.schedulers.Schedulers.from(exec);

        rx.Scheduler fj1 = rx.schedulers.Schedulers.from(ForkJoinPool.commonPool());
        io.reactivex.Scheduler fj2 = io.reactivex.schedulers.Schedulers.from(ForkJoinPool.commonPool());
        
        ps = rx.subjects.PublishSubject.create();
        
        pp = io.reactivex.processors.PublishProcessor.create();

        observableExec = ps.onBackpressureBuffer().observeOn(s1);
        
        observableFJ = ps.onBackpressureBuffer().observeOn(fj1);
        
        flowableExec = pp.onBackpressureBuffer().observeOn(s2);
        
        flowableFJ = pp.onBackpressureBuffer().observeOn(fj2);
    }

    @TearDown
    public void teardown() {
        exec.shutdownNow();
    }
    
    void await(CountDownLatch cdl, int c) throws Exception {
        if (c <= 1000) {
            while (cdl.getCount() != 0L) ;
        } else {
            cdl.await(5, TimeUnit.SECONDS);
        }
    }
    
    @Benchmark
    public void plainExecutor(Blackhole bh) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        int c = count;
        
        for (int i = 1; i <= c; i++) {
            int j = i;
            Callable<Integer> call = () -> {
                if (j == c) {
                    cdl.countDown();
                } else {
                    bh.consume(j);
                }
                return null;
            };
            
            exec.submit(call);
        }
        
        await(cdl, c);
    }
    
    @Benchmark
    public void plainForkJoin(Blackhole bh) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        int c = count;
        
        ExecutorService fj = ForkJoinPool.commonPool();
        
        for (int i = 1; i <= c; i++) {
            int j = i;
            Callable<Integer> call = () -> {
                if (j == c) {
                    cdl.countDown();
                } else {
                    bh.consume(j);
                }
                return null;
            };
            
            fj.submit(call);
        }
        
        await(cdl, c);
    }
    
    @Benchmark
    public void observableExecutor(Blackhole bh) throws Exception {
        int c = count;
        CountDownLatch cdl = new CountDownLatch(1);
        
        rx.Subscription s = observableExec.subscribe(j -> {
            if (j == c) {
                cdl.countDown();
            } else {
                bh.consume(j);
            }
        });
        
        for (int i = 1; i <= c; i++) {
            pp.onNext(i);
        }
        
        await(cdl, c);
        s.unsubscribe();
    }
    
    @Benchmark
    public void observableForkJoin(Blackhole bh) throws Exception {
        int c = count;
        CountDownLatch cdl = new CountDownLatch(1);
        
        rx.Subscription s = observableFJ.subscribe(j -> {
            if (j == c) {
                cdl.countDown();
            } else {
                bh.consume(j);
            }
        });
        
        for (int i = 1; i <= c; i++) {
            pp.onNext(i);
        }
        
        await(cdl, c);
        s.unsubscribe();
    }
    
    @Benchmark
    public void flowableExecutor(Blackhole bh) throws Exception {
        int c = count;
        CountDownLatch cdl = new CountDownLatch(1);
        
        Disposable d = flowableExec.subscribe(j -> {
            if (j == c) {
                cdl.countDown();
            } else {
                bh.consume(j);
            }
        });
        
        for (int i = 1; i <= c; i++) {
            pp.onNext(i);
        }
        
        await(cdl, c);
        d.dispose();
    }
    
    @Benchmark
    public void flowableForkJoin(Blackhole bh) throws Exception {
        int c = count;
        CountDownLatch cdl = new CountDownLatch(1);
        
        Disposable d = flowableFJ.subscribe(j -> {
            if (j == c) {
                cdl.countDown();
            } else {
                bh.consume(j);
            }
        });
        
        for (int i = 1; i <= c; i++) {
            pp.onNext(i);
        }
        
        await(cdl, c);
        d.dispose();
    }
}
