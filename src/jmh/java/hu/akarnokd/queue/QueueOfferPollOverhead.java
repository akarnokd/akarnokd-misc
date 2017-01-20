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

import java.util.*;
import java.util.concurrent.*;

import org.jctools.queues.*;
import org.openjdk.jmh.annotations.*;

import io.reactivex.internal.queue.SpscLinkedArrayQueue;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class QueueOfferPollOverhead {
    @Param({ "1", "1000", "1000000" })
    public int times;

    @Param({ /*"1", "2", "4", "8", "16", "32", "64",*/ "128" })
    public int capacity;

//    @Benchmark
    public void mpsc1() {
        MpscLinkedArrayQueue<Integer> q = new MpscLinkedArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
            q.poll();
        }
    }

//    @Benchmark
    public void mpscFill1() {
        MpscLinkedArrayQueue<Integer> q = new MpscLinkedArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
        }
        for (int i = 0; i < n; i++) {
            q.poll();
        }
    }

//    @Benchmark
    public void mpmc1() {
        FAAArrayQueue<Integer> q = new FAAArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.enqueue(0);
            q.dequeue();
        }
    }

//    @Benchmark
    public void mpmcFill1() {
        FAAArrayQueue<Integer> q = new FAAArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.enqueue(0);
        }
        for (int i = 0; i < n; i++) {
            q.dequeue();
        }
    }

//  @Benchmark
  public void mpmc1a() {
      FAAArrayQueueV2<Integer> q = new FAAArrayQueueV2<>(capacity);
      int n = times;
      for (int i = 0; i < n; i++) {
          q.enqueue(0);
          q.dequeue();
      }
  }

//  @Benchmark
  public void mpmcFill1a() {
      FAAArrayQueueV2<Integer> q = new FAAArrayQueueV2<>(capacity);
      int n = times;
      for (int i = 0; i < n; i++) {
          q.enqueue(0);
      }
      for (int i = 0; i < n; i++) {
          q.dequeue();
      }
  }

//    @Benchmark
    public void mpmc2() {
        Queue<Integer> q = new LinkedList<>();
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
            q.poll();
        }
    }

//    @Benchmark
    public void mpmcFill2() {
        Queue<Integer> q = new LinkedList<>();
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
        }
        for (int i = 0; i < n; i++) {
            q.poll();
        }
    }

//    @Benchmark
    public void mpmc3() {
        Queue<Integer> q = new ConcurrentLinkedQueue<>();
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
            q.poll();
        }
    }

//    @Benchmark
    public void mpmcFill3() {
        Queue<Integer> q = new ConcurrentLinkedQueue<>();
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
        }
        for (int i = 0; i < n; i++) {
            q.poll();
        }
    }

//    @Benchmark
    public void mpsc2() {
        MpscChunkedArrayQueue<Integer> q = new MpscChunkedArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
            q.poll();
        }
    }

//    @Benchmark
    public void mpscFill2() {
        MpscChunkedArrayQueue<Integer> q = new MpscChunkedArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
        }
        for (int i = 0; i < n; i++) {
            q.poll();
        }
    }

//    @Benchmark
    public void spsc1() {
        SpscLinkedArrayQueue<Integer> q = new SpscLinkedArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
            q.poll();
        }
    }

//    @Benchmark
    public void spscFill1() {
        SpscLinkedArrayQueue<Integer> q = new SpscLinkedArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
        }
        for (int i = 0; i < n; i++) {
            q.poll();
        }
    }


//    @Benchmark
    public void spsc2() {
        SpscChunkedArrayQueue<Integer> q = new SpscChunkedArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
            q.poll();
        }
    }

//    @Benchmark
    public void spscFill2() {
        SpscChunkedArrayQueue<Integer> q = new SpscChunkedArrayQueue<>(capacity);
        int n = times;
        for (int i = 0; i < n; i++) {
            q.offer(0);
        }
        for (int i = 0; i < n; i++) {
            q.poll();
        }
    }

}