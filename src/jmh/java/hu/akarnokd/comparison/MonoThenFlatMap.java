package hu.akarnokd.comparison;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import hu.akarnokd.rxjava2.basetypes.*;
import io.reactivex.*;
import reactor.core.publisher.Mono;

/**
 * Benchmark the Zip operator.
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class MonoThenFlatMap {

    @Benchmark
    public Object monoThen() {
        return Mono.just("").then(v -> Mono.just(v)).block();
    }

    @Benchmark
    public Object monoFlatMapLast() {
        return Mono.just("").flatMap(v -> Mono.just(v)).blockLast();
    }

    @Benchmark
    public Object monoFlatMapFirst() {
        return Mono.just("").flatMap(v -> Mono.just(v)).blockFirst();
    }

    @Benchmark
    public Object soloFlatMap() {
        return Solo.just("").flatMap(v -> Solo.just(v)).blockingGet();
    }

    @Benchmark
    public Object singleFlatMap() {
        return Single.just("").flatMap(v -> Single.just(v)).blockingGet();
    }

    @Benchmark
    public Object perhapsFlatMap() {
        return Perhaps.just("").flatMap(v -> Perhaps.just(v)).blockingGet();
    }

    @Benchmark
    public Object maybeFlatMap() {
        return Maybe.just("").flatMap(v -> Maybe.just(v)).blockingGet();
    }
}
