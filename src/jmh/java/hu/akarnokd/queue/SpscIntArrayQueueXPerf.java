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

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Control;

import rx.internal.util.unsafe.SpscArrayQueue;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Threads(2)
@State(Scope.Group)
public class SpscIntArrayQueueXPerf {
    
    @Param({ "1", "16", "128", "1024" })
    public int capacity;

    SpscIntArrayQueue queue;
    SpscArrayQueue<Integer> queueBoxed;
    SpscIntArrayQueueUnsafe queueUnsafe;
    SpscIntArrayQueueAtomic queueFastflow;
    
    final boolean[] hasValue = new boolean[1];
    
    @Setup(Level.Iteration)
    public void setup() {
        queue = new SpscIntArrayQueue(capacity);
        queueFastflow = new SpscIntArrayQueueAtomic(capacity);
        queueUnsafe = new SpscIntArrayQueueUnsafe(capacity);
        queueBoxed = new SpscArrayQueue<>(capacity);
    }
    
    @Group("primitive")
    @GroupThreads(1)
    @Benchmark
    public void send1(Control control) {
        SpscIntArrayQueue q = queue;
        while (!q.offer(1) && !control.stopMeasurement) {
        }
    }
    
    @Group("primitive")
    @GroupThreads(1)
    @Benchmark
    public void recv1(Control control) {
        final SpscIntArrayQueue q = queue;
        while (!control.stopMeasurement && q.poll() == 0);
    }
    
    @Group("unsafe")
    @GroupThreads(1)
    @Benchmark
    public void send3(Control control) {
        SpscIntArrayQueueUnsafe q = queueUnsafe;
        while (!q.offer(1) && !control.stopMeasurement) {
        }
    }
    
    @Group("unsafe")
    @GroupThreads(1)
    @Benchmark
    public void recv3(Control control) {
        final SpscIntArrayQueueUnsafe q = queueUnsafe;
        while (!control.stopMeasurement && q.poll() == 0);
    }
    
    @Group("fastflow")
    @GroupThreads(1)
    @Benchmark
    public void send4(Control control) {
        SpscIntArrayQueueAtomic q = queueFastflow;
        while (!q.offer(1) && !control.stopMeasurement) {
        }
    }
    
    @Group("fastflow")
    @GroupThreads(1)
    @Benchmark
    public void recv4(Control control) {
        final SpscIntArrayQueueAtomic q = queueFastflow;
        while (!control.stopMeasurement && q.poll() == 0);
    }
    
    @Group("boxed")
    @GroupThreads(1)
    @Benchmark
    public void send2(Control control) {
        SpscArrayQueue<Integer> q = queueBoxed;
        while (!q.offer(1) && !control.stopMeasurement) {
        }
    }
    
    @Group("boxed")
    @GroupThreads(1)
    @Benchmark
    public void recv2(Control control) {
        SpscArrayQueue<Integer> q = queueBoxed;
        while (!control.stopMeasurement && q.poll() == null) {
        }
    }
}