package hu.akarnokd.rxjava4;

import java.util.concurrent.*;
import java.util.function.BiConsumer;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.rxjava4.core.Streamer;

@State(Scope.Benchmark)
public class WhenCompletePerf implements BiConsumer<Object, Throwable> {

    static Blackhole bh;
    
    @org.openjdk.jmh.annotations.Setup
    public void setup(Blackhole bh) {
        WhenCompletePerf.bh = bh;
    }
    
    @Override
    public void accept(Object t, Throwable u) {
        bh.consume(t);
        bh.consume(u);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(value = 1, jvmArgs = {
            "-XX:MaxInlineLevel=20"
//            , "-XX:+UnlockDiagnosticVMOptions",
//            , "-XX:+PrintAssembly",
//            , "-XX:+TraceClassLoading",
//            , "-XX:+LogCompilation"
    })
    public Object standard() {
        return Streamer.NEXT_TRUE.whenComplete(this);
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(value = 1, jvmArgs = {
            "-XX:MaxInlineLevel=20"
//            , "-XX:+UnlockDiagnosticVMOptions",
//            , "-XX:+PrintAssembly",
//            , "-XX:+TraceClassLoading",
//            , "-XX:+LogCompilation"
    })
    public Object inlined() {
        return ALT_TRUE.whenComplete(this);
    }

    static final TrueCompletableFuture ALT_TRUE = new TrueCompletableFuture();

    static final class TrueCompletableFuture extends CompletableFuture<Boolean> {
        TrueCompletableFuture() {
            super();
            complete(true);
        }

        @Override
        public CompletableFuture<Boolean> whenComplete(BiConsumer<? super Boolean, ? super Throwable> action) {
            try {
                action.accept(true, null);
            } catch (Throwable ex) {
                return CompletableFuture.failedFuture(ex);
            }
            return this;
        }
    }
}
