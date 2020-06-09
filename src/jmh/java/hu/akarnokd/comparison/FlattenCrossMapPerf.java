package hu.akarnokd.comparison;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfConsumer;
import io.helidon.common.reactive.Multi;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class FlattenCrossMapPerf {
    @Param({ "1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int times;

    Flowable<Integer> flowable;

    Flowable<Integer> flowable2;

    Observable<Integer> observable;

    Multi<Integer> multi;

    @Setup
    public void setup() {
        Integer[] array = new Integer[times];
        Arrays.fill(array, 777);

        Integer[] arrayInner = new Integer[1000000 / times];
        Arrays.fill(arrayInner, 888);

        final Iterable<Integer> outer = Arrays.asList(array);
        final Iterable<Integer> inner = Arrays.asList(arrayInner);

        flowable = Flowable.fromIterable(outer).flatMapIterable(new Function<Integer, Iterable<Integer>>() {
            @Override
            public Iterable<Integer> apply(Integer v) {
                return inner;
            }
        });

        flowable2 = Flowable.fromIterable(outer).hide().flatMapIterable(new Function<Integer, Iterable<Integer>>() {
            @Override
            public Iterable<Integer> apply(Integer v) {
                return inner;
            }
        });

        observable = Observable.fromIterable(outer).flatMapIterable(new Function<Integer, Iterable<Integer>>() {
            @Override
            public Iterable<Integer> apply(Integer v) {
                return inner;
            }
        });

        multi = Multi.create(outer).flatMapIterable(v -> inner, 128);
    }

//    @Benchmark
    public void flowable(Blackhole bh) {
        flowable.subscribe(new PerfConsumer(bh));
    }

//    @Benchmark
    public void flowable2(Blackhole bh) {
        flowable2.subscribe(new PerfConsumer(bh));
    }

//    @Benchmark
    public void observable(Blackhole bh) {
        observable.subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void multi(Blackhole bh) {
        multi.subscribe(new PerfConsumer(bh));
    }
}