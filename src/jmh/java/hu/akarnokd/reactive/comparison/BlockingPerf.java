package hu.akarnokd.reactive.comparison;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import reactor.core.publisher.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class BlockingPerf {
    
    rx.Observable<Integer> firstRxObservable;
    
    rx.observables.BlockingObservable<Integer> firstRxBlockingObservable;
    
    rx.Observable<Integer> lastRxObservable;
    
    rx.observables.BlockingObservable<Integer> lastRxBlockingObservable;
    
    rx.Single<Integer> firstRxSingle;
    
    rx.singles.BlockingSingle<Integer> firstRxBlockingSingle;
    
    rx.Completable emptyRxCompletable;
    
    io.reactivex.Observable<Integer> firstRx2Observable;
    
    io.reactivex.Observable<Integer> lastRx2Observable;

    io.reactivex.Flowable<Integer> firstRx2Flowable;
    
    io.reactivex.Flowable<Integer> lastRx2Flowable;

    io.reactivex.Single<Integer> firstRx2Single;
    
    io.reactivex.Completable emptyRx2Completable;

    io.reactivex.Maybe<Integer> firstRx2Maybe;

    io.reactivex.Maybe<Integer> emptyRx2Maybe;

    Flux<Integer> firstReactorFlux;
    
    Flux<Integer> lastReactorFlux;
    
    Mono<Integer> firstReactorMono;
    
    Mono<Integer> emptyReactorMono;
    
    @Setup
    public void setup() {
        
        firstRxObservable = rx.Observable.just(1);
        firstRxBlockingObservable = firstRxObservable.toBlocking();
        
        Integer[] array = new Integer[1_000_000];
        Arrays.fill(array, 777);
        
        lastRxObservable = rx.Observable.from(array);
        lastRxBlockingObservable = lastRxObservable.toBlocking();
        
        firstRxSingle = rx.Single.just(1);

        firstRxBlockingSingle = firstRxSingle.toBlocking();

        emptyRxCompletable = rx.Completable.complete();
        
        firstRx2Observable = io.reactivex.Observable.just(1);
        
        lastRx2Observable = io.reactivex.Observable.fromArray(array);
        
        firstRx2Flowable = io.reactivex.Flowable.just(1);
        
        lastRx2Flowable = io.reactivex.Flowable.fromArray(array);
        
        firstRx2Single = io.reactivex.Single.just(1);
        
        emptyRx2Completable = io.reactivex.Completable.complete();

        firstRx2Maybe = io.reactivex.Maybe.just(1);
        
        emptyRx2Maybe = io.reactivex.Maybe.empty();
        
        firstReactorFlux = Flux.just(1);
        
        lastReactorFlux = Flux.fromArray(array);
        
        firstReactorMono = Mono.just(1);
        
        emptyReactorMono = Mono.empty();
    }
    
    @Benchmark
    public Object firstRxObservable(Blackhole bh) {
        return firstRxObservable.toBlocking().first();
    }
    
    @Benchmark
    public Object firstRxBlockingObservable(Blackhole bh) {
        return firstRxBlockingObservable.first();
    }
    
    @Benchmark
    public Object lastRxObservable(Blackhole bh) {
        return lastRxObservable.toBlocking().last();
    }
    
    @Benchmark
    public Object lastRxBlockingObservable(Blackhole bh) {
        return lastRxBlockingObservable.last();
    }
    
    @Benchmark
    public Object firstRxSingle(Blackhole bh) {
        return firstRxSingle.toBlocking().value();
    }

    @Benchmark
    public Object firstRxBlockingSingle(Blackhole bh) {
        return firstRxBlockingSingle.value();
    }

    @Benchmark
    public Object emptyRxCompletable(Blackhole bh) {
        return emptyRxCompletable.get();
    }
    
    @Benchmark
    public Object firstRx2Observable(Blackhole bh) {
        return firstRx2Observable.blockingFirst();
    }
    
    @Benchmark
    public Object lastRx2Observable(Blackhole bh) {
        return lastRx2Observable.blockingLast();
    }

    @Benchmark
    public Object firstRx2Flowable(Blackhole bh) {
        return firstRx2Flowable.blockingFirst();
    }
    
    @Benchmark
    public Object lastRx2Flowable(Blackhole bh) {
        return lastRx2Flowable.blockingLast();
    }

    @Benchmark
    public Object firstRx2Single(Blackhole bh) {
        return firstRx2Single.blockingGet();
    }
    
    @Benchmark
    public Object emptyRx2Completable(Blackhole bh) {
        return emptyRx2Completable.blockingGet();
    }

    @Benchmark
    public Object firstRx2Maybe(Blackhole bh) {
        return firstRx2Maybe.blockingGet();
    }

    @Benchmark
    public Object emptyRx2Maybe(Blackhole bh) {
        return emptyRx2Maybe.blockingGet();
    }
    
    @Benchmark
    public Object firstReactorFlux() {
        return firstReactorFlux.blockFirst();
    }
    
    @Benchmark
    public Object lastReactorFlux() {
        return lastReactorFlux.blockLast();
    }
    
    @Benchmark
    public Object firstReactorMono() {
        return firstReactorMono.block();
    }
    
    @Benchmark
    public Object emptyReactorMono() {
        return emptyReactorMono.block();
    }

}
