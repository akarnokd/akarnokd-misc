package hu.akarnokd.iterables;

import java.util.*;
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
@Fork(value = 1, jvmArgs = {
        "-XX:MaxInlineLevel=20",
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+PrintAssembly",
        "-XX:+TraceClassLoading",
        "-XX:+LogCompilation" })
@State(Scope.Thread)
public class LambdaSingletonPerf {

    List<Integer> arrayList;
    
    Iterable<Integer> lambda;

    Iterable<Integer> lambdaBugged;
    
    Ix<Integer> ix;
    
    Iterable<Integer> singleton;

    Iterable<Integer> singletonList;

    Consumer<Integer> c;
    
    @Setup
    public void setup(Blackhole bh) {
        arrayList = new ArrayList<>();
        arrayList.add(1);
        lambda = SingletonSet.of(1);
        lambdaBugged = SingletonSetBugged.of(1);
        ix = Ix.just(1);
        singleton = Collections.singleton(1);
        singletonList = Collections.singletonList(1);
        c = bh::consume;
    }

//    @Benchmark
    public void arrayList() {
        arrayList.forEach(c);
    }

    @Benchmark
    public void lambda() {
        lambda.forEach(c);
    }

//    @Benchmark
    public void lambdaBugged() {
        lambdaBugged.forEach(c);
    }

//    @Benchmark
    public void ix() {
        ix.forEach(c);
    }

//    @Benchmark
    public void singleton() {
        singleton.forEach(c);
    }

//    @Benchmark
    public void singletonList() {
        singletonList.forEach(c);
    }

//    @Benchmark
    public void arrayListFor(Blackhole bh) {
        for (Integer i : arrayList) {
            bh.consume(i);
        }
    }

//    @Benchmark
    public void lambdaFor(Blackhole bh) {
        for (Integer i : lambda) {
            bh.consume(i);
        }
    }

//    @Benchmark
    public void lambdaBuggedFor(Blackhole bh) {
        for (Integer i : lambdaBugged) {
            bh.consume(i);
        }
    }

//    @Benchmark
    public void ixFor(Blackhole bh) {
        for (Integer i : ix) {
            bh.consume(i);
        }
    }

//    @Benchmark
    public void singletonFor(Blackhole bh) {
        for (Integer i : singleton) {
            bh.consume(i);
        }
    }

//    @Benchmark
    public void singletonListFor(Blackhole bh) {
        for (Integer i : singletonList) {
            bh.consume(i);
        }

    }
    
    static volatile Object field;
    
    public static void main(String[] args) {
        SingletonSet<Integer> set = SingletonSet.of(1);
        for (int i = 0; i < 500_000_000; i++) {
            for (Object s : set) {
                field = s;
            }
        }
    }
}
