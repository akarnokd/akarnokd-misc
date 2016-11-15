package hu.akarnokd.comparison;

import java.util.Arrays;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import io.reactivex.*;
import io.reactivex.internal.schedulers.SingleScheduler;
import io.reactivex.schedulers.Schedulers;
import io.windmill.core.*;
import reactor.core.publisher.Flux;
import rsc.publisher.Px;
import rsc.scheduler.ExecutorScheduler;

/**
 * Benchmark the Windmill library.
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class WindmillPerf {

    @State(Scope.Thread)
    public static class WindmillState {
        @Param({"1", "1000", "1000000"})
        public int count;

        private CPUSet cs;

        CPU cpu1;

        CPU cpu2;

        Publisher<Integer> rscWindmill;

        rx.Observable<Integer> rx1Windmill;

        Publisher<Integer> rx2Windmill;

        @Setup
        public void setup() {
            cs = CPUSet.builder().addSocket(0, 1).build();

            cs.start();

            cpu1 = cs.get(0);
            cpu2 = cs.get(1);

            Scheduler s1 = Schedulers.from(r -> cpu1.schedule(r::run));
            Scheduler s2 = Schedulers.from(r -> cpu2.schedule(r::run));

            Integer[] arr = new Integer[count];
            Arrays.fill(arr, 777);

            rx2Windmill = Flowable.fromArray(arr).subscribeOn(s1).observeOn(s2);

            rx.Scheduler s3 = rx.schedulers.Schedulers.from(r -> cpu1.schedule(r::run));
            rx.Scheduler s4 = rx.schedulers.Schedulers.from(r -> cpu2.schedule(r::run));

            rx1Windmill = rx.Observable.from(arr).subscribeOn(s3).observeOn(s4);

            rsc.scheduler.Scheduler scheduler1 = new ExecutorScheduler(r -> cpu1.schedule(r::run), false);

            rsc.scheduler.Scheduler scheduler2 = new ExecutorScheduler(r -> cpu2.schedule(r::run), false);

            rscWindmill = Px.fromArray(arr).subscribeOn(scheduler1).observeOn(scheduler2);
        }
        @TearDown
        public void teardown() {
            cs.halt();
        }
    }

    @State(Scope.Thread)
    public static class ExecutorState {
        @Param({"1", "1000", "1000000"})
        public int count;

        Publisher<Integer> rsc;


        Publisher<Integer> rx2;


        rx.Observable<Integer> rx1;

        private ExecutorService exec1;

        private ExecutorService exec2;

        @Setup
        public void setup() {
            exec1 = Executors.newSingleThreadExecutor();
            exec2 = Executors.newSingleThreadExecutor();

            Integer[] arr = new Integer[count];
            Arrays.fill(arr, 777);

            rx2 = Flowable.fromArray(arr).subscribeOn(new SingleScheduler()).observeOn(Schedulers.single());

            rx1 = rx.Observable.from(arr).subscribeOn(rx.schedulers.Schedulers.computation()).observeOn(rx.schedulers.Schedulers.computation());

            rsc = Px.fromArray(arr).subscribeOn(exec1).observeOn(exec2);

        }
        @TearDown
        public void teardown() {
            exec1.shutdown();
            exec2.shutdown();
        }
    }

    @State(Scope.Thread)
    public static class ReactorState {
        @Param({"1", "1000", "1000000"})
        public int count;

        Flux<Integer> flx;

        private reactor.core.scheduler.Scheduler sg1;

        private reactor.core.scheduler.Scheduler sg2;

        @Setup
        public void setup() {
            sg1 = reactor.core.scheduler.Schedulers.newSingle("A");
            sg2 = reactor.core.scheduler.Schedulers.newSingle("B");

            Integer[] arr = new Integer[count];
            Arrays.fill(arr, 777);

            flx = Flux.fromArray(arr).subscribeOn(sg1).publishOn(sg2);
        }

        @TearDown
        public void teardown() {
            sg1.shutdown();
            sg2.shutdown();
        }
    }


    static void await(int count, CountDownLatch latch) throws Exception {
        if (count < 1000) {
            while (latch.getCount() != 0) {
                ;
            }
        } else {
            latch.await();
        }
    }


    @Benchmark
    public void rsc(ExecutorState state, Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        state.rsc.subscribe(o);
        await(state.count, o.latch);
    }

    @Benchmark
    public void Flux(ReactorState state, Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        state.flx.subscribe(o);
        await(state.count, o.latch);
    }

    @Benchmark
    public void rscWindmill(WindmillState state, Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        state.rscWindmill.subscribe(o);

        await(state.count, o.latch);
    }

    @Benchmark
    public void rx2(ExecutorState state, Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        state.rx2.subscribe(o);

        await(state.count, o.latch);
    }

    @Benchmark
    public void rx2Windmill(WindmillState state, Blackhole bh) throws Exception {
        LatchedRSObserver<Integer> o = new LatchedRSObserver<>(bh);
        state.rx2Windmill.subscribe(o);

        await(state.count, o.latch);
    }

    @Benchmark
    public void rx1(ExecutorState state, Blackhole bh) throws Exception {
        LatchedObserver<Integer> o = new LatchedObserver<>(bh);
        state.rx1.subscribe(o);

        await(state.count, o.latch);
    }

    @Benchmark
    public void rx1Windmill(WindmillState state, Blackhole bh) throws Exception {
        LatchedObserver<Integer> o = new LatchedObserver<>(bh);
        state.rx1Windmill.subscribe(o);

        await(state.count, o.latch);
    }

    @Benchmark
    public void executor(ExecutorState state, Blackhole bh) throws Exception {

        CountDownLatch cdl = new CountDownLatch(1);

        int c = state.count;
        for (int i = 0; i < c; i++) {
            int j = i;
            state.exec1.submit(() -> {
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }

        await(c, cdl);
    }

    @Benchmark
    public void forkjoin(ExecutorState state, Blackhole bh) throws Exception {

        CountDownLatch cdl = new CountDownLatch(1);

        ForkJoinPool fj = ForkJoinPool.commonPool();

        int c = state.count;
        for (int i = 0; i < c; i++) {
            int j = i;
            fj.submit(() -> {
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }

        await(c, cdl);
    }


    @Benchmark
    public void windmill(WindmillState state, Blackhole bh) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);

        int c = state.count;
        for (int i = 0; i < c; i++) {
            int j = i;
            state.cpu1.schedule(() -> {
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }

        await(c, cdl);
    }

    @Benchmark
    public void reactor(ReactorState state, Blackhole bh) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);

        int c = state.count;
        for (int i = 0; i < c; i++) {
            int j = i;
            state.sg1.schedule(() -> {
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }

        await(c, cdl);
    }
}
