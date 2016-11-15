package hu.akarnokd.comparison;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.Flowable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

/**
 * Benchmark the Zip operator.
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ZipPerf {

    @Param({"1", "1000", "1000000"})
    public int firstLen;
    @Param({"1", "1000", "1000000"})
    public int secondLen;

    Flowable<Integer> baseline;

    Flowable<Integer> bothSync;
    Flowable<Integer> firstSync;
    Flowable<Integer> secondSync;
    Flowable<Integer> bothAsync;

    boolean small;

    @Setup
    public void setup() {
        Integer[] array1 = new Integer[firstLen];
        Arrays.fill(array1, 777);
        Integer[] array2 = new Integer[secondLen];
        Arrays.fill(array2, 777);

        baseline = Flowable.fromArray(firstLen < secondLen ? array2 : array1);

        Flowable<Integer> o1 = Flowable.fromArray(array1);

        Flowable<Integer> o2 = Flowable.fromArray(array2);

        BiFunction<Integer, Integer, Integer> plus = (a, b) -> a + b;

        bothSync = Flowable.zip(o1, o2, plus);

        firstSync = Flowable.zip(o1, o2.subscribeOn(Schedulers.computation()), plus);

        secondSync = Flowable.zip(o1.subscribeOn(Schedulers.computation()), o2, plus);

        bothAsync = Flowable.zip(o1.subscribeOn(Schedulers.computation()), o2.subscribeOn(Schedulers.computation()), plus);

        small = Math.min(firstLen, secondLen) < 100;
    }

    @Benchmark
    public void baseline(Blackhole bh) {
        baseline.subscribe(new LatchedRSObserver<Integer>(bh));
    }

    @Benchmark
    public void syncSync(Blackhole bh) {
        bothSync.subscribe(new LatchedRSObserver<Integer>(bh));
    }

    @Benchmark
    public void syncAsync(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        firstSync.subscribe(o);

        if (small) {
            while (o.latch.getCount() != 0) {
                ;
            }
        } else {
            o.latch.await();
        }
    }

    @Benchmark
    public void asyncSync(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        secondSync.subscribe(o);

        if (small) {
            while (o.latch.getCount() != 0) {
                ;
            }
        } else {
            o.latch.await();
        }
    }

    @Benchmark
    public void asyncAsync(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        bothAsync.subscribe(o);

        if (small) {
            while (o.latch.getCount() != 0) {
                ;
            }
        } else {
            o.latch.await();
        }
    }

}
