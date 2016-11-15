package hu.akarnokd.reactive.comparison;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Processor;

import reactor.core.publisher.DirectProcessor;
import reactor.util.concurrent.QueueSupplier;

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
        run(reactor.core.publisher.UnicastProcessor.create(QueueSupplier.<Integer>unbounded(128).get()), bh);
    }
}
