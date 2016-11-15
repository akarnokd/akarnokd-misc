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

import rx.internal.util.unsafe.SpscArrayQueue;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SpscIntArrayQueuePerf {

    @Param({ "1", "1000", "1000000" })
    public int times;

    @Param({ "1", "16", "128", "1024" })
    public int capacity;

    SpscIntArrayQueue queue;
    SpscArrayQueue<Integer> queueBoxed;

    static final Integer VALUE = 777;

    @Setup
    public void setup() {
        queue = new SpscIntArrayQueue(capacity);
        queueBoxed = new SpscArrayQueue<>(capacity);
    }

    @Benchmark
    public void offerPollPrimitive() {
        int s = times;
        SpscIntArrayQueue q = queue;

        for (int i = 0; i < s; i++) {
            q.offer(777 + i);
            q.poll();
        }
    }

//    @Benchmark
    public void offerPollObject() {
        int s = times;
        SpscArrayQueue<Integer> q = queueBoxed;

        for (int i = 0; i < s; i++) {
            q.offer(777 + i);
            q.poll();
        }
    }

//    @Benchmark
    public void offerPollConst() {
        int s = times;
        SpscArrayQueue<Integer> q = queueBoxed;

        for (int i = 0; i < s; i++) {
            q.offer(VALUE);
            q.poll();
        }
    }

    @Benchmark
    public void offerCapacityPrimitive() {
        if (times != 1) {
            throw new RuntimeException("Skip this settings");
        }
        int s = capacity;
        SpscIntArrayQueue q = queue;

        for (int i = 0; i < s; i++) {
            q.offer(777 + i);
        }
        for (int i = 0; i < s; i++) {
            q.poll();
        }
    }

//    @Benchmark
    public void offerCapacityObject() {
        if (times != 1) {
            throw new RuntimeException("Skip this settings");
        }
        int s = capacity;
        SpscArrayQueue<Integer> q = queueBoxed;

        for (int i = 0; i < s; i++) {
            q.offer(777 + i);
        }
        for (int i = 0; i < s; i++) {
            q.poll();
        }
    }

//    @Benchmark
    public void offerCapacityConst() {
        if (times != 1) {
            throw new RuntimeException("Skip this settings");
        }
        int s = capacity;
        SpscArrayQueue<Integer> q = queueBoxed;

        for (int i = 0; i < s; i++) {
            q.offer(VALUE);
        }
        for (int i = 0; i < s; i++) {
            q.poll();
        }
    }
}