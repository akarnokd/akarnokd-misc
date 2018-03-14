package hu.akarnokd.reactive.comparison.rx1;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.*;

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
        rxCompletableNever.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void emptyRxCompletable(Blackhole bh) {
        rxCompletableEmpty.subscribe(new PerfConsumer(bh));
    }


    @Benchmark
    public void justRxCompletable(Blackhole bh) {
        rxCompletableJust.subscribe(new PerfConsumer(bh));
    }

    /// ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

}
