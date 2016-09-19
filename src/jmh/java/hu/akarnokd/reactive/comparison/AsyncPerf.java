package hu.akarnokd.reactive.comparison;

import java.util.Arrays;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import io.reactivex.*;
import reactor.core.publisher.Flux;
import rx.schedulers.Schedulers;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class AsyncPerf {
    
    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    rx.Observable<Integer> asyncRxObservable;
    
    rx.Observable<Integer> pipelineRxObservable;
    
    io.reactivex.Observable<Integer> asyncRx2Observable;
    
    io.reactivex.Observable<Integer> pipelineRx2Observable;
    
    io.reactivex.Flowable<Integer> asyncRx2Flowable;
    
    io.reactivex.Flowable<Integer> pipelineRx2Flowable;
    
    Flux<Integer> asyncReactorFlux;
    
    Flux<Integer> pipelineReactorFlux;

    ExecutorService exec = Executors.newSingleThreadExecutor();

    ExecutorService exec2 = Executors.newSingleThreadExecutor();
    
    @Setup
    public void setup() {
        Integer[] array = new Integer[count];
        Arrays.fill(array, 777);
        
        rx.Observable<Integer> arrayRx = rx.Observable.from(array);
        
        asyncRxObservable = arrayRx.observeOn(Schedulers.from(exec));
        
        pipelineRxObservable = arrayRx.subscribeOn(Schedulers.from(exec2)).observeOn(Schedulers.from(exec));
        
        
        Flowable<Integer> arrayRx2F = Flowable.fromArray(array);
        
        asyncRx2Flowable = arrayRx2F.observeOn(io.reactivex.schedulers.Schedulers.from(exec));
        
        pipelineRx2Flowable = arrayRx2F.subscribeOn(io.reactivex.schedulers.Schedulers.from(exec2)).observeOn(io.reactivex.schedulers.Schedulers.from(exec));
        
        
        io.reactivex.Observable<Integer> arrayRx2O = io.reactivex.Observable.fromArray(array);
        
        asyncRx2Observable = arrayRx2O.observeOn(io.reactivex.schedulers.Schedulers.from(exec));
        
        pipelineRx2Observable = arrayRx2O.subscribeOn(io.reactivex.schedulers.Schedulers.from(exec2)).observeOn(io.reactivex.schedulers.Schedulers.from(exec));
        
        Flux<Integer> arrayFlux = Flux.fromArray(array);
        
        asyncReactorFlux = arrayFlux.publishOn(reactor.core.scheduler.Schedulers.fromExecutor(exec));
        
        pipelineReactorFlux = arrayFlux.subscribeOn(reactor.core.scheduler.Schedulers.fromExecutor(exec2)).publishOn(reactor.core.scheduler.Schedulers.fromExecutor(exec));
    }
    
    @TearDown
    public void teardown() {
        exec.shutdown();
        exec2.shutdown();
    }
    
    final void run(Publisher<?> p, Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        p.subscribe(s);
        s.await(count);
    }
    
    final void run(ObservableSource<?> p, Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        p.subscribe(s);
        s.await(count);
    }
    
    final void run(rx.Observable<?> p, Blackhole bh) {
        PerfRxAsyncSubscriber s = new PerfRxAsyncSubscriber(bh);
        p.subscribe(s);
        s.await(count);
    }
    
    @Benchmark
    public void asyncRxObservable(Blackhole bh) {
        run(asyncRxObservable, bh);
    }
    
    @Benchmark
    public void pipelineRxObservable(Blackhole bh) {
        run(pipelineRxObservable, bh);
    }
    
    @Benchmark
    public void asyncRx2Observable(Blackhole bh) {
        run(asyncRx2Observable, bh);
    }
    
    @Benchmark
    public void pipelineRx2Observable(Blackhole bh) {
        run(pipelineRx2Observable, bh);
    }
    
    @Benchmark
    public void asyncRx2Flowable(Blackhole bh) {
        run(asyncRx2Flowable, bh);
    }
    
    @Benchmark
    public void pipelineRx2Flowable(Blackhole bh) {
        run(pipelineRx2Flowable, bh);
    }
    
    @Benchmark
    public void asyncReactorFlux(Blackhole bh) {
        run(asyncReactorFlux, bh);
    }
    
    @Benchmark
    public void pipelineReactorFlux(Blackhole bh) {
        run(pipelineReactorFlux, bh);
    }

}
