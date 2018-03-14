package hu.akarnokd.reactive.comparison.reactor;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Processor;

import hu.akarnokd.reactive.comparison.consumers.*;
import reactor.core.publisher.DirectProcessor;
import reactor.util.concurrent.*;

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

    final void run(Processor<Integer, Integer> subject, Blackhole bh) {
        subject.subscribe(new PerfConsumer(bh));
        int e = count;
        for (int i = 0; i < e; i++) {
            subject.onNext(1);
        }
        subject.onComplete();
        bh.consume(subject);
    }

    @Benchmark
    public void rangeReactorDirectProcessor(Blackhole bh) {
        run(DirectProcessor.create(), bh);
    }

    @Benchmark
    public void rangeReactorReplayProcessor(Blackhole bh) {
        run(reactor.core.publisher.ReplayProcessor.create(128, false), bh);
    }

    @Benchmark
    public void rangeReactorUnicastProcessor(Blackhole bh) {
        run(reactor.core.publisher.UnicastProcessor.create(Queues.<Integer>unbounded(128).get()), bh);
    }
}
