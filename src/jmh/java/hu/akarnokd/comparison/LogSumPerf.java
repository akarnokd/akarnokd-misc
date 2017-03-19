package hu.akarnokd.comparison;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import io.reactivex.schedulers.Schedulers;

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
public class LogSumPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    public int count;

    rx.Observable<Long> rx1Observable;

    io.reactivex.Observable<Long> rx2Observable;

    io.reactivex.Flowable<Long> rx2Flowable;

    io.reactivex.Flowable<Long> rx2Parallel;

    @Setup
    public void setup() {
        Integer[] array = new Integer[count];
        for (int i = 0; i < count; i++) {
            array[i] = i;
        }

        rx1Observable = rx.Observable.from(array)
                .map(v -> v == 0 ? (long)Math.log10(v) + 1 : 1L)
                .compose(o -> rx.observables.MathObservable.sumLong(o))
                ;

        rx2Observable = io.reactivex.Observable.fromArray(array)
                .map(v -> v == 0 ? (long)Math.log10(v) + 1 : 1L)
                .compose(o -> hu.akarnokd.rxjava2.math.MathObservable.sumLong(o))
                ;

        rx2Flowable = io.reactivex.Flowable.fromArray(array)
                .map(v -> v == 0 ? (long)Math.log10(v) + 1 : 1L)
                .compose(f -> hu.akarnokd.rxjava2.math.MathFlowable.sumLong(f))
                ;

        rx2Parallel = io.reactivex.Flowable.fromArray(array)
                .parallel()
                .runOn(Schedulers.computation())
                .map(v -> v == 0 ? (long)Math.log10(v) + 1 : 1L)
                .reduce((a, b) -> a + b)
                ;
    }

    @Benchmark
    public Object rx1Observable() {
        return rx1Observable.toBlocking().last();
    }

    @Benchmark
    public Object rx2Observable() {
        return rx2Observable.blockingLast();
    }

    @Benchmark
    public Object rx2Flowable() {
        return rx2Flowable.blockingLast();
    }

    @Benchmark
    public Object rx2Parallel() {
        return rx2Parallel.blockingLast();
    }

}
