package hu.akarnokd.reactive.comparison.reactor;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.*;
import reactor.core.publisher.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SubscribePerf {

    Flux<Integer> reactorFluxNever = Flux.never();

    Flux<Integer> reactorFluxEmpty = Flux.empty();

    Flux<Integer> reactorFluxJust = Flux.just(1);

    Mono<Integer> reactorMonoNever = Mono.never();

    Mono<Integer> reactorMonoEmpty = Mono.empty();

    Mono<Integer> reactorMonoJust = Mono.just(1);

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
