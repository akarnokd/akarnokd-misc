package hu.akarnokd.atomics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class TerminalAtomics {

    final AtomicReference<Object> ref = new AtomicReference<>();

    final Object o = new Object();

    void getGetAndSet(Blackhole bh) {
        Object o = ref.get();
        if (o != null) {
            o = ref.getAndSet(null);
            if (o != null) {
                bh.consume(o);
            }
        }
    }

    void getAndSet(Blackhole bh) {
        Object o = ref.getAndSet(null);
        if (o != null) {
            bh.consume(o);
        }
    }

    @Benchmark
    public void GGASx1(Blackhole bh) {
        ref.lazySet(o);
        getGetAndSet(bh);
    }

    @Benchmark
    public void GGASx2(Blackhole bh) {
        ref.lazySet(o);
        getGetAndSet(bh);
        getGetAndSet(bh);
    }

    @Benchmark
    public void GGASx3(Blackhole bh) {
        ref.lazySet(o);
        getGetAndSet(bh);
        getGetAndSet(bh);
        getGetAndSet(bh);
    }

    @Benchmark
    public void GGASx4(Blackhole bh) {
        ref.lazySet(o);
        getGetAndSet(bh);
        getGetAndSet(bh);
        getGetAndSet(bh);
        getGetAndSet(bh);
    }

    @Benchmark
    public void GASx1(Blackhole bh) {
        ref.lazySet(o);
        getAndSet(bh);
    }

    @Benchmark
    public void GASx2(Blackhole bh) {
        ref.lazySet(o);
        getAndSet(bh);
        getAndSet(bh);
    }

    @Benchmark
    public void GASx3(Blackhole bh) {
        ref.lazySet(o);
        getAndSet(bh);
        getAndSet(bh);
        getAndSet(bh);
    }

    @Benchmark
    public void GASx4(Blackhole bh) {
        ref.lazySet(o);
        getAndSet(bh);
        getAndSet(bh);
        getAndSet(bh);
        getAndSet(bh);
    }
}
