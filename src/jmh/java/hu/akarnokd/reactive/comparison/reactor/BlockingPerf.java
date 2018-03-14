package hu.akarnokd.reactive.comparison.reactor;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import reactor.core.publisher.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class BlockingPerf {

    Flux<Integer> firstReactorFlux;

    Flux<Integer> lastReactorFlux;

    Mono<Integer> firstReactorMono;

    Mono<Integer> emptyReactorMono;

    Flux<Integer> emptyReactorFlux;

    @Setup
    public void setup() {

        Integer[] array = new Integer[1_000_000];
        Arrays.fill(array, 777);

        firstReactorFlux = Flux.just(1);

        lastReactorFlux = Flux.fromArray(array);

        firstReactorMono = Mono.just(1);

        emptyReactorMono = Mono.empty();

        emptyReactorFlux = Flux.empty();
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

    @Benchmark
    public Object emptyReactorFlux() {
        return emptyReactorFlux.blockLast();
    }

}
