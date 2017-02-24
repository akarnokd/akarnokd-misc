package hu.akarnokd.reactive;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh='SubscriberManagerPerf'
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ItemManagerPerf {

    @Param({ "0", "1", "2", "4", "8", "16", "32", "64", "96" })
    public int shift;

    LazyItemManager<Integer> v1;
    CowItemManager<Integer> v2;

    @Setup
    public void setup() {
        v1 = new LazyItemManager<>(128);
        v2 = new CowItemManager<>();
    }

    @Benchmark
    public void offerRemoveV1() {
        int s = shift;
        LazyItemManager<Integer> a = v1;
        
        for (int i = 0; i < 128; i++) {
            a.offer(i);
            int j = i - s;
            if (j >= 0) {
                a.remove(j);
            }
        }

        for (int j = 128 - s; j < 128; j++) {
            a.remove(j);
        }
    }

    @Benchmark
    public void offerRemoveV2() {
        int s = shift;
        CowItemManager<Integer> a = v2;
        
        for (int i = 0; i < 128; i++) {
            a.offer(i);
            int j = i - s;
            if (j >= 0) {
                a.remove(j);
            }
        }
        
        for (int j = 128 - s; j < 128; j++) {
            a.remove(j);
        }
    }
}
