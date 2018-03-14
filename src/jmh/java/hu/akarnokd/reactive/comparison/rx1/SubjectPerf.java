package hu.akarnokd.reactive.comparison.rx1;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfRxSubscriber;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SubjectPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    final void run(rx.subjects.Subject<Integer, Integer> subject, Blackhole bh) {
        subject.subscribe(new PerfRxSubscriber(bh));
        int e = count;
        for (int i = 0; i < e; i++) {
            subject.onNext(1);
        }
        subject.onCompleted();
        bh.consume(subject);
    }

    @Benchmark
    public void rangeRxAsyncSubject(Blackhole bh) {
        run(rx.subjects.AsyncSubject.create(), bh);
    }

    @Benchmark
    public void rangeRxPublishSubject(Blackhole bh) {
        run(rx.subjects.PublishSubject.create(), bh);
    }

    @Benchmark
    public void rangeRxBehaviorSubject(Blackhole bh) {
        run(rx.subjects.BehaviorSubject.create(), bh);
    }

    @Benchmark
    public void rangeRxReplaySubject(Blackhole bh) {
        run(rx.subjects.ReplaySubject.create(), bh);
    }

    @Benchmark
    public void rangeRxUnicastSubject(Blackhole bh) {
        run(rx.subjects.UnicastSubject.create(), bh);
    }

    // ************************************************************************

}
