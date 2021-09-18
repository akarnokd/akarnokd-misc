package hu.akarnokd.reactive.comparison.reactor;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfConsumer;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SubjectPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    // ************************************************************************

    final void run(Sinks.Many<Integer> subject, Blackhole bh) {
        subject.asFlux().subscribe(new PerfConsumer(bh));
        int e = count;
        for (int i = 0; i < e; i++) {
            subject.tryEmitNext(1);
        }
        subject.tryEmitComplete();
        bh.consume(subject);
    }

    @Benchmark
    public void rangeReactorDirectProcessor(Blackhole bh) {
        run(Sinks.many().multicast().directBestEffort(), bh);
    }

    @Benchmark
    public void rangeReactorReplayProcessor(Blackhole bh) {
        run(Sinks.many().replay().all(128), bh);
    }

    @Benchmark
    public void rangeReactorUnicastProcessor(Blackhole bh) {
        run(Sinks.many().unicast().onBackpressureBuffer(Queues.<Integer>unbounded(128).get()), bh);
    }
}
