package hu.akarnokd.reactive.comparison.rx2;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfConsumer;
import io.reactivex.Flowable;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SubscribePerf {

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
}
