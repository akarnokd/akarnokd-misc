/*
 * Copyright 2015 David Karnok
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package hu.akarnokd.queue;

import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import hu.akarnokd.comparison.LatchedRSObserver;
import rsc.publisher.*;
import rsc.scheduler.ExecutorServiceScheduler;
import rsc.util.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class QueuePerf {
    @Param({ "1", "1000", "1000000" })
    public int count;
    
    @Param({ "16", "256", "1024", "65536"})
    public int prefetch;

    ExecutorService exec1;
    ExecutorService exec2;

    Px<Integer> rsc;
    Px<Integer> rscLinked;
    
    Px<Integer> jctUnsafe;
    Px<Integer> jctSafe;
    
    Px<Integer> jctUnsafeGrow;
//    Px<Integer> jctSafeGrow;

    Px<Integer> jctUnsafeUnb;
    Px<Integer> jctSafeUnb;

    
    @Setup
    public void setup() {
        exec1 = Executors.newSingleThreadExecutor();
        exec2 = Executors.newSingleThreadExecutor();
        
        Integer[] arr = new Integer[count];
        for (int i = 0; i < count; i++) {
            arr[i] = 777;
        }
        
        Px<Integer> source = Px.fromArray(arr).subscribeOn(exec1);
        
        ExecutorServiceScheduler s2 = new ExecutorServiceScheduler(exec2);
        
        rsc = new PublisherObserveOn<>(source, s2, false, prefetch, () -> new SpscArrayQueue<>(prefetch));
        
        rscLinked = new PublisherObserveOn<>(source, s2, false, prefetch, () -> new SpscLinkedArrayQueue<>(prefetch));

        jctUnsafe = new PublisherObserveOn<>(source, s2, false, prefetch, () -> new org.jctools.queues.SpscArrayQueue<>(prefetch));
        jctSafe = new PublisherObserveOn<>(source, s2, false, prefetch, () -> new org.jctools.queues.atomic.SpscAtomicArrayQueue<>(prefetch));

        jctUnsafeGrow = new PublisherObserveOn<>(source, s2, false, prefetch, () -> new org.jctools.queues.SpscGrowableArrayQueue<>(prefetch));
//        jctSafeGrow = new PublisherObserveOn<>(source, s2, false, prefetch, () -> new org.jctools.queues.atomic.SpscGrowableAtomicArrayQueue<>(prefetch));

        jctUnsafeUnb = new PublisherObserveOn<>(source, s2, false, prefetch, () -> new org.jctools.queues.SpscUnboundedArrayQueue<>(prefetch));
        jctSafeUnb = new PublisherObserveOn<>(source, s2, false, prefetch, () -> new org.jctools.queues.atomic.SpscUnboundedAtomicArrayQueue<>(prefetch));

    }
    
    @TearDown
    public void teardown() {
        exec1.shutdown();
        exec2.shutdown();
    }
    
    void run(Publisher<Integer> p, Blackhole bh) {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        
        p.subscribe(lo);
        
        if (count < 1000) {
            while (lo.latch.getCount() != 0);
        } else {
            try {
                lo.latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Benchmark
    public void rsc(Blackhole bh) {
        run(rsc, bh);
    }

    @Benchmark
    public void rscLinked(Blackhole bh) {
        run(rscLinked, bh);
    }

    @Benchmark
    public void jctUnsafe(Blackhole bh) {
        run(jctUnsafe, bh);
    }

    @Benchmark
    public void jctSafe(Blackhole bh) {
        run(jctSafe, bh);
    }

    @Benchmark
    public void jctUnsafeGrow(Blackhole bh) {
        run(jctUnsafeGrow, bh);
    }

//    @Benchmark
//    public void jctSafeGrow(Blackhole bh) {
//        run(jctSafeGrow, bh);
//    }

    @Benchmark
    public void jctUnsafeUnb(Blackhole bh) {
        run(jctUnsafeUnb, bh);
    }

    @Benchmark
    public void jctSafeUnb(Blackhole bh) {
        run(jctSafeUnb, bh);
    }
}