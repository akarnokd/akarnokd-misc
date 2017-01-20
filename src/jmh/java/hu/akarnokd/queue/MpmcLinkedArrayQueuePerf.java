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

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Threads(2)
@State(Scope.Group)
public class MpmcLinkedArrayQueuePerf {

    @Param({ "1", "16", "128", "1024" })
    public int capacity;

    FAAArrayQueue<Integer> q4;

    FAAArrayQueueV2<Integer> q5;

    @Setup(Level.Iteration)
    public void setup() {
        q4 = new FAAArrayQueue<>(capacity);
        q5 = new FAAArrayQueueV2<>(capacity);
    }

    @Group("mpmca")
    @GroupThreads(1)
    @Benchmark
    public void send4(Control control) {
        final FAAArrayQueue<Integer> q = q4;
        q.enqueue(1);
    }

    @Group("mpmca")
    @GroupThreads(1)
    @Benchmark
    public void recv4(Control control) {
        final FAAArrayQueue<Integer> q = q4;
        while (!control.stopMeasurement && q.dequeue() == null) {
            ;
        }
    }

    @Group("mpmcb")
    @GroupThreads(1)
    @Benchmark
    public void send5(Control control) {
        final FAAArrayQueueV2<Integer> q = q5;
        q.enqueue(1);
    }

    @Group("mpmcb")
    @GroupThreads(1)
    @Benchmark
    public void recv5(Control control) {
        final FAAArrayQueueV2<Integer> q = q5;
        while (!control.stopMeasurement && q.dequeue() == null) {
            ;
        }
    }

}