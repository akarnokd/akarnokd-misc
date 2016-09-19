package hu.akarnokd.reactive.comparison;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.Flowable;
import reactor.core.publisher.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SubscribePerf {
    
    rx.Observable<Integer> rxObservableNever = rx.Observable.never();
    
    rx.Single<Integer> rxSingleNever = rx.Single.create(new rx.Single.OnSubscribe<Integer>() {
        @Override
        public void call(rx.SingleSubscriber<? super Integer> t) {
            
        }
    });
    
    rx.Completable rxCompletableNever = rx.Completable.never();
    
    rx.Observable<Integer> rxObservableEmpty = rx.Observable.empty();
    
    rx.Single<Integer> rxSingleEmpty = rx.Single.error(new RuntimeException());
    
    rx.Completable rxCompletableEmpty = rx.Completable.complete();
    
    rx.Observable<Integer> rxObservableJust = rx.Observable.just(1);
    
    rx.Single<Integer> rxSingleJust = rx.Single.just(1);
    
    rx.Completable rxCompletableJust = rx.Completable.error(new RuntimeException());
    
    // ---------------------------------------------
    
    io.reactivex.Flowable<Integer> rx2FlowableNever = Flowable.never();

    io.reactivex.Observable<Integer> rx2ObservableNever = io.reactivex.Observable.never();

    io.reactivex.Single<Integer> rx2SingleNever = io.reactivex.Single.never();

    io.reactivex.Completable rx2CompletableNever = io.reactivex.Completable.never();

    io.reactivex.Maybe<Integer> rx2MaybeNever = io.reactivex.Maybe.never();

    io.reactivex.Flowable<Integer> rx2FlowableEmpty = Flowable.empty();

    io.reactivex.Observable<Integer> rx2ObservableEmpty = io.reactivex.Observable.empty();

    io.reactivex.Single<Integer> rx2SingleEmpty = io.reactivex.Single.error(new RuntimeException());

    io.reactivex.Completable rx2CompletableEmpty = io.reactivex.Completable.complete();

    io.reactivex.Maybe<Integer> rx2MaybeEmpty = io.reactivex.Maybe.empty();

    io.reactivex.Flowable<Integer> rx2FlowableJust = Flowable.just(1);

    io.reactivex.Observable<Integer> rx2ObservableJust = io.reactivex.Observable.just(1);

    io.reactivex.Single<Integer> rx2SingleJust = io.reactivex.Single.just(1);

    io.reactivex.Completable rx2CompletableJust = io.reactivex.Completable.error(new RuntimeException());

    io.reactivex.Maybe<Integer> rx2MaybeJust = io.reactivex.Maybe.just(1);

    // ---------------------------------------------

    Flux<Integer> reactorFluxNever = Flux.never();
    
    Flux<Integer> reactorFluxEmpty = Flux.empty();
    
    Flux<Integer> reactorFluxJust = Flux.just(1);
    
    Mono<Integer> reactorMonoNever = Mono.never();
    
    Mono<Integer> reactorMonoEmpty = Mono.empty();
    
    Mono<Integer> reactorMonoJust = Mono.just(1);
    
    /// ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
    
    @Benchmark
    public void neverRxObservable(Blackhole bh) {
        rxObservableNever.subscribe(new PerfRxSubscriber(bh));
    }
    
    @Benchmark
    public void emptyRxObservable(Blackhole bh) {
        rxObservableEmpty.subscribe(new PerfRxSubscriber(bh));
    }
    
    
    @Benchmark
    public void justRxObservable(Blackhole bh) {
        rxObservableJust.subscribe(new PerfRxSubscriber(bh));
    }

    @Benchmark
    public void neverRxSingle(Blackhole bh) {
        rxSingleNever.subscribe(new PerfRxSingleSubscriber(bh));
    }
    
    @Benchmark
    public void emptyRxSingle(Blackhole bh) {
        rxSingleEmpty.subscribe(new PerfRxSingleSubscriber(bh));
    }
    
    
    @Benchmark
    public void justRxSingle(Blackhole bh) {
        rxSingleJust.subscribe(new PerfRxSingleSubscriber(bh));
    }

    @Benchmark
    public void neverRxCompletable(Blackhole bh) {
        rxCompletableNever.subscribe(new PerfRxCompletableSubscriber(bh));
    }
    
    @Benchmark
    public void emptyRxCompletable(Blackhole bh) {
        rxCompletableEmpty.subscribe(new PerfRxCompletableSubscriber(bh));
    }
    
    
    @Benchmark
    public void justRxCompletable(Blackhole bh) {
        rxCompletableJust.subscribe(new PerfRxCompletableSubscriber(bh));
    }
    
    /// ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
    
    @Benchmark
    public void neverRx2Observable(Blackhole bh) {
        rx2ObservableNever.subscribe(new PerfConsumer(bh));
    }
    
    @Benchmark
    public void emptyRx2Observable(Blackhole bh) {
        rx2ObservableEmpty.subscribe(new PerfConsumer(bh));
    }
    
    
    @Benchmark
    public void justRx2Observable(Blackhole bh) {
        rx2ObservableJust.subscribe(new PerfConsumer(bh));
    }
    
    @Benchmark
    public void neverRx2Flowable(Blackhole bh) {
        rx2FlowableNever.subscribe(new PerfConsumer(bh));
    }
    
    @Benchmark
    public void emptyRx2Flowable(Blackhole bh) {
        rx2FlowableEmpty.subscribe(new PerfConsumer(bh));
    }
    
    
    @Benchmark
    public void justRx2Flowable(Blackhole bh) {
        rx2FlowableJust.subscribe(new PerfConsumer(bh));
    }


    @Benchmark
    public void neverRx2Single(Blackhole bh) {
        rx2SingleNever.subscribe(new PerfConsumer(bh));
    }
    
    @Benchmark
    public void emptyRx2Single(Blackhole bh) {
        rx2SingleEmpty.subscribe(new PerfConsumer(bh));
    }
    
    
    @Benchmark
    public void justRx2Single(Blackhole bh) {
        rx2SingleJust.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void neverRx2Completable(Blackhole bh) {
        rx2CompletableNever.subscribe(new PerfConsumer(bh));
    }
    
    @Benchmark
    public void emptyRx2Completable(Blackhole bh) {
        rx2CompletableEmpty.subscribe(new PerfConsumer(bh));
    }
    
    
    @Benchmark
    public void justRx2Completable(Blackhole bh) {
        rx2CompletableJust.subscribe(new PerfConsumer(bh));
    }


    @Benchmark
    public void neverRx2Maybe(Blackhole bh) {
        rx2MaybeNever.subscribe(new PerfConsumer(bh));
    }
    
    @Benchmark
    public void emptyRx2Maybe(Blackhole bh) {
        rx2MaybeEmpty.subscribe(new PerfConsumer(bh));
    }
    
    
    @Benchmark
    public void justRx2Maybe(Blackhole bh) {
        rx2MaybeJust.subscribe(new PerfConsumer(bh));
    }
    
    /// ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
    
    @Benchmark
    public void neverReactorFlux(Blackhole bh) {
        reactorFluxNever.subscribe(new PerfConsumer(bh));
    }
    
    @Benchmark
    public void emptyReactorFlux(Blackhole bh) {
        reactorFluxEmpty.subscribe(new PerfConsumer(bh));
    }
    
    
    @Benchmark
    public void justReactorFlux(Blackhole bh) {
        reactorFluxJust.subscribe(new PerfConsumer(bh));
    }
    
    
    @Benchmark
    public void neverReactorMono(Blackhole bh) {
        reactorMonoNever.subscribe(new PerfConsumer(bh));
    }
    
    @Benchmark
    public void emptyReactorMono(Blackhole bh) {
        reactorMonoEmpty.subscribe(new PerfConsumer(bh));
    }
    
    
    @Benchmark
    public void justReactorMono(Blackhole bh) {
        reactorMonoJust.subscribe(new PerfConsumer(bh));
    }
}
