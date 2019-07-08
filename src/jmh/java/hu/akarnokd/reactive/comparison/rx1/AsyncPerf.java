package hu.akarnokd.reactive.comparison.rx1;

import java.util.Arrays;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfRxAsyncSubscriber;
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

    ExecutorService exec = Executors.newSingleThreadExecutor();

    ExecutorService exec2 = Executors.newSingleThreadExecutor();

    @Setup
    public void setup() {
        Integer[] array = new Integer[count];
        Arrays.fill(array, 777);

        rx.Observable<Integer> arrayRx = rx.Observable.from(array);

        asyncRxObservable = arrayRx.observeOn(Schedulers.from(exec));

        pipelineRxObservable = arrayRx.subscribeOn(Schedulers.from(exec2)).observeOn(Schedulers.from(exec));
    }

    @TearDown
    public void teardown() {
        exec.shutdown();
        exec2.shutdown();
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
}
