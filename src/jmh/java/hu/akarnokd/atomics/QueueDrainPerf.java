package hu.akarnokd.atomics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class QueueDrainPerf {

    long counter;

    final AtomicInteger wip = new AtomicInteger();

    @Benchmark
    public void queueDrainAtomic1() {
        if (wip.getAndIncrement() == 0) {
            int missed = 1;

            for (;;) {
                counter++;
                
                missed = wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

    @Benchmark
    public void queueDrainAtomic2() {
        AtomicInteger w = wip;
        if (w.getAndIncrement() == 0) {
            int missed = 1;

            for (;;) {
                counter++;
                
                missed = w.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

    @Benchmark
    public void queueDrainAtomic3() {
        AtomicInteger w = wip;
        if (w.getAndIncrement() == 0) {
            int missed = 1;

            for (;;) {
                counter++;
                
                int u = w.get();
                if (missed == u) {
                    missed = w.addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = u;
                }
            }
        }
    }

    @Benchmark
    public void queueDrainAtomic4() {
        AtomicInteger w = wip;
        if (w.getAndIncrement() == 0) {
            int missed = 1;

            for (;;) {
                counter++;

                if (w.compareAndSet(missed, 0)) {
                    break;
                }
                missed = w.get();
            }
        }
    }

    @Benchmark
    public void queueDrainAtomic5() {
        AtomicInteger w = wip;
        if (w.compareAndSet(0, 1) || w.getAndIncrement() == 0) {
            int missed = 1;

            for (;;) {
                counter++;

                if (w.compareAndSet(missed, 0)) {
                    break;
                }
                missed = w.get();
            }
        }
    }
}
