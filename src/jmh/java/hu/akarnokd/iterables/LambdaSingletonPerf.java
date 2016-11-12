package hu.akarnokd.iterables;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import ix.Ix;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh='LambdaSingletonPerf'
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class LambdaSingletonPerf {

    Iterable<Integer> lambda;

    Iterable<Integer> lambdaBugged;
    
    Ix<Integer> ix;
    
    Iterable<Integer> singleton;

    Iterable<Integer> singletonList;

    Consumer<Integer> c;
    
    @Setup
    public void setup(Blackhole bh) {
        lambda = SingletonSet.of(1);
        lambdaBugged = SingletonSetBugged.of(1);
        ix = Ix.just(1);
        singleton = Collections.singleton(1);
        singletonList = Collections.singletonList(1);
        c = bh::consume;
    }

    @Benchmark
    public void lambda() {
        lambda.forEach(c);
    }

    @Benchmark
    public void lambdaBugged() {
        lambdaBugged.forEach(c);
    }

    @Benchmark
    public void ix() {
        ix.forEach(c);
    }

    @Benchmark
    public void singleton() {
        singleton.forEach(c);
    }

    @Benchmark
    public void singletonList() {
        singletonList.forEach(c);
    }
}
