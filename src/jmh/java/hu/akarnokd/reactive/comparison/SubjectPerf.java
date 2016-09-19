package hu.akarnokd.reactive.comparison;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Processor;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SubjectPerf {
    
    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;
    
    rx.subjects.AsyncSubject<Integer> rangeRxAsyncSubject;

    rx.subjects.PublishSubject<Integer> rangeRxPublishSubject;

    rx.subjects.BehaviorSubject<Integer> rangeRxBehaviorSubject;
    
    rx.subjects.ReplaySubject<Integer> rangeRxReplaySubject;
    
    rx.subjects.UnicastSubject<Integer> rangeRxUnicastSubject;
    
    // ************************************************************************
    
    io.reactivex.subjects.AsyncSubject<Integer> rangeRx2AsyncSubject;

    io.reactivex.subjects.PublishSubject<Integer> rangeRx2PublishSubject;

    io.reactivex.subjects.BehaviorSubject<Integer> rangeRx2BehaviorSubject;
    
    io.reactivex.subjects.ReplaySubject<Integer> rangeRx2ReplaySubject;
    
    io.reactivex.subjects.UnicastSubject<Integer> rangeRx2UnicastSubject;
    
    // ************************************************************************

    io.reactivex.processors.AsyncProcessor<Integer> rangeRx2AsyncProcessor;

    io.reactivex.processors.PublishProcessor<Integer> rangeRx2PublishProcessor;

    io.reactivex.processors.BehaviorProcessor<Integer> rangeRx2BehaviorProcessor;
    
    io.reactivex.processors.ReplayProcessor<Integer> rangeRx2ReplayProcessor;

    io.reactivex.processors.UnicastProcessor<Integer> rangeRx2UnicastProcessor;

    // ************************************************************************

    //reactor.core.publisher.AsyncProcessor<Integer> rangeReactorAsyncProcessor;

    reactor.core.publisher.DirectProcessor<Integer> rangeReactorDirectProcessor;

    //reactor.core.publisher.BehaviorProcessor<Integer> rangeReactorBehaviorProcessor;
    
    reactor.core.publisher.ReplayProcessor<Integer> rangeReactorReplayProcessor;

    reactor.core.publisher.UnicastProcessor<Integer> rangeReactorUnicastProcessor;

    
    // ************************************************************************

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
        run(rangeRxAsyncSubject, bh);
    }

    @Benchmark
    public void rangeRxPublishSubject(Blackhole bh) {
        run(rangeRxPublishSubject, bh);
    }

    @Benchmark
    public void rangeRxBehaviorSubject(Blackhole bh) {
        run(rangeRxBehaviorSubject, bh);
    }
    
    @Benchmark
    public void rangeRxReplaySubject(Blackhole bh) {
        run(rangeRxReplaySubject, bh);
    }
    
    @Benchmark
    public void rangeRxUnicastSubject(Blackhole bh) {
        run(rangeRxUnicastSubject, bh);
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
        run(rangeRx2AsyncSubject, bh);
    }

    @Benchmark
    public void rangeRx2PublishSubject(Blackhole bh) {
        run(rangeRx2PublishSubject, bh);
    }

    @Benchmark
    public void rangeRx2BehaviorSubject(Blackhole bh) {
        run(rangeRx2BehaviorSubject, bh);
    }
    
    @Benchmark
    public void rangeRx2ReplaySubject(Blackhole bh) {
        run(rangeRx2ReplaySubject, bh);
    }
    
    @Benchmark
    public void rangeRx2UnicastSubject(Blackhole bh) {
        run(rangeRx2UnicastSubject, bh);
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
        run(rangeRx2AsyncProcessor, bh);
    }

    @Benchmark
    public void rangeRx2PublishProcessor(Blackhole bh) {
        run(rangeRx2PublishProcessor, bh);
    }

    @Benchmark
    public void rangeRx2BehaviorProcessor(Blackhole bh) {
        run(rangeRx2BehaviorProcessor, bh);
    }
    
    @Benchmark
    public void rangeRx2ReplayProcessor(Blackhole bh) {
        run(rangeRx2ReplayProcessor, bh);
    }

    @Benchmark
    public void rangeRx2UnicastProcessor(Blackhole bh) {
        run(rangeRx2UnicastProcessor, bh);
    }

    // ************************************************************************

    @Benchmark
    public void rangeReactorDirectProcessor(Blackhole bh) {
        run(rangeReactorDirectProcessor, bh);
    }

    @Benchmark
    public void rangeReactorReplayProcessor(Blackhole bh) {
        run(rangeReactorReplayProcessor, bh);
    }

    @Benchmark
    public void rangeReactorUnicastProcessor(Blackhole bh) {
        run(rangeReactorUnicastProcessor, bh);
    }
}
