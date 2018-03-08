/**
 * Copyright (c) 2016-present, RxJava Contributors.
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

package hu.akarnokd.rxjava2;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.disposables.*;
import io.reactivex.internal.disposables.DisposableContainer;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class DisposableContainerPerf {

    @Param({/*"1", */"2", "3", "4"})
    public int concurrency;
    
    @Param({"1", "10", "100", "1000"})
    public int work;

    @Param({"1000000"})
    public int calls;

    ExecutorService exec;

    @Setup
    public void setup() {
        exec = Executors.newFixedThreadPool(concurrency);
    }

    @TearDown
    public void teardown() {
        exec.shutdownNow();
    }

    void doWork(DisposableContainer dc, CountDownLatch cdl, AtomicInteger race) {
        int c = calls;
        int w = work;
        if (race.decrementAndGet() != 0) {
            while (race.get() != 0) { }
        }
        for (int i = 0; i < c; i++) {
            Disposable d = Disposables.empty();
            dc.add(d);
            Blackhole.consumeCPU(w);
            dc.delete(d);
        }
        cdl.countDown();
    }

    @Benchmark
    public Object compositeAsync() throws Exception {
        CountDownLatch cdl = new CountDownLatch(concurrency);
        CompositeDisposable cd = new CompositeDisposable();
        AtomicInteger race = new AtomicInteger(concurrency);
        Runnable r = () -> doWork(cd, cdl, race);
        for (int i = 0; i < concurrency; i++) {
            exec.submit(r);
        }
        cdl.await();
        return cd;
    }

    @Benchmark
    public Object fixedAsync() throws Exception {
        CountDownLatch cdl = new CountDownLatch(concurrency);
        FixedCompositeDisposable cd = new FixedCompositeDisposable(concurrency);
        AtomicInteger race = new AtomicInteger(concurrency);
        Runnable r = () -> doWork(cd, cdl, race);
        for (int i = 0; i < concurrency; i++) {
            exec.submit(r);
        }
        cdl.await();
        return cd;
    }

    @Benchmark
    public Object stripedAsync() throws Exception {
        CountDownLatch cdl = new CountDownLatch(concurrency);
        StripedCompositeDisposable cd = new StripedCompositeDisposable(concurrency);
        AtomicInteger race = new AtomicInteger(concurrency);
        Runnable r = () -> doWork(cd, cdl, race);
        for (int i = 0; i < concurrency; i++) {
            exec.submit(r);
        }
        cdl.await();
        return cd;
    }
}
