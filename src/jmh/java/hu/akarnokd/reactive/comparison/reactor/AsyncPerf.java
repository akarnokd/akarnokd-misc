package hu.akarnokd.reactive.comparison.reactor;

import java.util.Arrays;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import hu.akarnokd.reactive.comparison.consumers.PerfAsyncConsumer;
import reactor.core.publisher.Flux;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class AsyncPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    Flux<Integer> asyncReactorFlux;

    Flux<Integer> pipelineReactorFlux;

    ExecutorService exec = Executors.newSingleThreadExecutor();

    ExecutorService exec2 = Executors.newSingleThreadExecutor();

    @Setup
    public void setup() {
        Integer[] array = new Integer[count];
        Arrays.fill(array, 777);

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

    @Benchmark
    public void asyncReactorFlux(Blackhole bh) {
        run(asyncReactorFlux, bh);
    }

    @Benchmark
    public void pipelineReactorFlux(Blackhole bh) {
        run(pipelineReactorFlux, bh);
    }

}
