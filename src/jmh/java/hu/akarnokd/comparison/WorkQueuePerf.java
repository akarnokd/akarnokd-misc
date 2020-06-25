package hu.akarnokd.comparison;

import java.util.Arrays;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Subscription;

import hu.akarnokd.rxjava2.processors.DispatchWorkProcessor;
import hu.akarnokd.rxjava2.subjects.DispatchWorkSubject;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.extra.processor.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
@SuppressWarnings("deprecation")
public class WorkQueuePerf {

    @Param({"1", "1000", "1000000" })
    public int count;

    @Param({"1", "2", "3", "4"})
    public int parallelism;

    @Param({"1", "100", "10000"})
    public int work;

    Integer[] array;

    @Setup
    public void setup() {
        array = new Integer[count];
        Arrays.fill(array, 777);
    }

    @TearDown
    public void teardown() {
    }

    @Benchmark
    public Object dispatchWorkSubject() throws Exception {
        DispatchWorkSubject<Integer> dws = DispatchWorkSubject.create(Schedulers.computation());
        CountDownLatch cdl = new CountDownLatch(parallelism);

        for (int i = 0; i < parallelism; i++) {
            dws.subscribe(new Observer<Integer>() {
                @Override
                public void onSubscribe(Disposable s) {
                }

                @Override
                public void onNext(Integer t) {
                    Blackhole.consumeCPU(work);
                }

                @Override
                public void onError(Throwable t) {
                    cdl.countDown();
                }

                @Override
                public void onComplete() {
                    cdl.countDown();
                }
            });
        }

        int c = count;
        Integer v = 777;
        for (int i = 0; i < c; i++) {
            dws.onNext(v);
        }
        dws.onComplete();

        if (count <= 1000) {
            while (cdl.getCount() != 0) { }
        } else {
            cdl.await();
        }

        return dws;
    }

    @Benchmark
    public Object dispatchWorkProcessor() throws Exception {
        DispatchWorkProcessor<Integer> dwp = DispatchWorkProcessor.create(Schedulers.computation());
        CountDownLatch cdl = new CountDownLatch(parallelism);

        for (int i = 0; i < parallelism; i++) {
            dwp.subscribe(new FlowableSubscriber<Integer>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Integer t) {
                    Blackhole.consumeCPU(work);
                }

                @Override
                public void onError(Throwable t) {
                    cdl.countDown();
                }

                @Override
                public void onComplete() {
                    cdl.countDown();
                }
            });
        }

        Flowable.fromArray(array).subscribe(dwp);

        if (count <= 1000) {
            while (cdl.getCount() != 0) { }
        } else {
            cdl.await();
        }

        return dwp;
    }

    @Benchmark
    public Object dispatchWorkProcessorUnbounded() throws Exception {
        DispatchWorkProcessor<Integer> dwp = DispatchWorkProcessor.createUnbounded(Schedulers.computation());
        CountDownLatch cdl = new CountDownLatch(parallelism);

        for (int i = 0; i < parallelism; i++) {
            dwp.subscribe(new FlowableSubscriber<Integer>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Integer t) {
                    Blackhole.consumeCPU(work);
                }

                @Override
                public void onError(Throwable t) {
                    cdl.countDown();
                }

                @Override
                public void onComplete() {
                    cdl.countDown();
                }
            });
        }

        int c = count;
        Integer v = 777;
        for (int i = 0; i < c; i++) {
            dwp.onNext(v);
        }
        dwp.onComplete();

        if (count <= 1000) {
            while (cdl.getCount() != 0) { }
        } else {
            cdl.await();
        }

        return dwp;
    }

    Object workQueueProcessor(WaitStrategy stategy) throws Exception {
        WorkQueueProcessor<Integer> wqp = WorkQueueProcessor.<Integer>builder()
                .bufferSize(Flowable.bufferSize())
                .executor(Executors.newFixedThreadPool(parallelism))
                .autoCancel(false)
                .waitStrategy(stategy)
                .build();

        CountDownLatch cdl = new CountDownLatch(parallelism);

        for (int i = 0; i < parallelism; i++) {
            wqp.subscribe(new CoreSubscriber<Integer>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Integer t) {
                    Blackhole.consumeCPU(work);
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    cdl.countDown();
                }

                @Override
                public void onComplete() {
                    cdl.countDown();
                }
            });
        }

        Flux.fromArray(array).subscribe(wqp);

        if (count <= 1000) {
            while (cdl.getCount() != 0) { }
        } else {
            cdl.await();
        }

        return wqp;
    }

    @Benchmark
    public Object workQueueProcessorParking() throws Exception {
        return workQueueProcessor(WaitStrategy.parking());
    }

    @Benchmark
    public Object workQueueProcessorBlocking() throws Exception {
        return workQueueProcessor(WaitStrategy.blocking());
    }

    @Benchmark
    public Object workQueueProcessorLiteBlocking() throws Exception {
        return workQueueProcessor(WaitStrategy.liteBlocking());
    }

    @Benchmark
    public Object workQueueProcessorBusySpin() throws Exception {
        return workQueueProcessor(WaitStrategy.busySpin());
    }

    @Benchmark
    public Object workQueueProcessorSleeping() throws Exception {
        return workQueueProcessor(WaitStrategy.sleeping());
    }

    @Benchmark
    public Object workQueueProcessorYielding() throws Exception {
        return workQueueProcessor(WaitStrategy.yielding());
    }

    @Benchmark
    public void flowableParallel(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> ls = Flowable.fromArray(array)
                .parallel(parallelism)
                .runOn(Schedulers.computation())
                .filter(v -> { Blackhole.consumeCPU(work); return false; })
                .sequential()
                .subscribeWith(new LatchedRSObserver<>(bh));

        if (count <= 1000) {
            while (ls.latch.getCount() != 0) { }
        } else {
            ls.latch.await();
        }
    }

    @Benchmark
    public void fluxParallel(Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> ls = Flux.fromArray(array)
                .parallel(parallelism)
                .runOn(reactor.core.scheduler.Schedulers.parallel())
                .filter(v -> { Blackhole.consumeCPU(work); return false; })
                .sequential()
                .subscribeWith(new LatchedRSObserver<>(bh));

        if (count <= 1000) {
            while (ls.latch.getCount() != 0) { }
        } else {
            ls.latch.await();
        }
    }

    public static void main(String[] args) throws Exception {
        WorkQueuePerf p = new WorkQueuePerf();
        p.count = 1000000;
        p.work = 10000;
        p.parallelism = 2;
        p.setup();
        for (int i = 1; i < 10; i++) {
            System.out.println("Run " + i);
            p.workQueueProcessorBlocking();
        }
        p.teardown();
    }
}
