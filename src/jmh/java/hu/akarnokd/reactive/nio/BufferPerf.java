package hu.akarnokd.reactive.nio;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh='IxPerf'
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class BufferPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    public int count;
    
    ByteBuffer buffer;

    @Setup
    public void setup() {
        buffer = ByteBuffer.allocate(count);
        buffer.clear();
    }
    
    @Benchmark
    public void indexLoop(Blackhole bh) {
        ByteBuffer bi = buffer;
        
        int c = bi.limit();
        for (int i = 0; i < c; i++) {
            bh.consume(bi.get(i));
        }
        bi.clear();
    }
    
    @Benchmark
    public void consumeLoop(Blackhole bh) {
        ByteBuffer bi = buffer;
        
        while (bi.hasRemaining()) {
            bh.consume(bi.get());
        }
        
        bi.clear();
    }
    
}
