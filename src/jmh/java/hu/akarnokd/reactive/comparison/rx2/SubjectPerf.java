package hu.akarnokd.reactive.comparison.rx2;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Processor;

import hu.akarnokd.reactive.comparison.consumers.PerfConsumer;

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

    final void run(io.reactivex.subjects.Subject<Integer> subject, Blackhole bh) {
        subject.subscribe(new PerfConsumer(bh));
        int e = count;
        for (int i = 0; i < e; i++) {
            subject.onNext(1);
        }
        subject.onComplete();
        bh.consume(subject);
    }

    @Benchmark
    public void rangeRx2AsyncSubject(Blackhole bh) {
        run(io.reactivex.subjects.AsyncSubject.create(), bh);
    }

    @Benchmark
    public void rangeRx2PublishSubject(Blackhole bh) {
        run(io.reactivex.subjects.PublishSubject.create(), bh);
    }

    @Benchmark
    public void rangeRx2BehaviorSubject(Blackhole bh) {
        run(io.reactivex.subjects.BehaviorSubject.create(), bh);
    }

    @Benchmark
    public void rangeRx2ReplaySubject(Blackhole bh) {
        run(io.reactivex.subjects.ReplaySubject.create(), bh);
    }

    @Benchmark
    public void rangeRx2UnicastSubject(Blackhole bh) {
        run(io.reactivex.subjects.UnicastSubject.create(), bh);
    }

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
    public void rangeRx2AsyncProcessor(Blackhole bh) {
        run(io.reactivex.processors.AsyncProcessor.create(), bh);
    }

    @Benchmark
    public void rangeRx2PublishProcessor(Blackhole bh) {
        run(io.reactivex.processors.PublishProcessor.create(), bh);
    }

    @Benchmark
    public void rangeRx2BehaviorProcessor(Blackhole bh) {
        run(io.reactivex.processors.BehaviorProcessor.create(), bh);
    }

    @Benchmark
    public void rangeRx2ReplayProcessor(Blackhole bh) {
        run(io.reactivex.processors.ReplayProcessor.create(), bh);
    }

    @Benchmark
    public void rangeRx2UnicastProcessor(Blackhole bh) {
        run(io.reactivex.processors.UnicastProcessor.create(), bh);
    }

    // ************************************************************************
}
