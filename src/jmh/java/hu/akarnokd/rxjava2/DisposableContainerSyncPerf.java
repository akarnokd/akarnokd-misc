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

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.disposables.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class DisposableContainerSyncPerf {

    @Param({"1000000"})
    public int calls;

    @Setup
    public void setup(Blackhole bh) {
    }

    @Benchmark
    public Object compositeSync() throws Exception {
        CompositeDisposable cd = new CompositeDisposable();
        int c = calls;
        for (int i = 0; i < c; i++) {
            Disposable d = Disposables.empty();
            cd.add(d);
            cd.delete(d);
        }
        return cd;
    }

    @Benchmark
    public Object compositeWithExisting() throws Exception {
        int n = Runtime.getRuntime().availableProcessors();
        CompositeDisposable cd = new CompositeDisposable();
        for (int i = 0; i < n - 1; i++) {
            cd.add(Disposables.empty());
        }
        int c = calls;
        for (int i = 0; i < c; i++) {
            Disposable d = Disposables.empty();
            cd.add(d);
            cd.delete(d);
        }
        return cd;
    }

    @Benchmark
    public Object fixedSync() throws Exception {
        int n = Runtime.getRuntime().availableProcessors();
        FixedCompositeDisposable cd = new FixedCompositeDisposable(n);
        int c = calls;
        for (int i = 0; i < c; i++) {
            Disposable d = Disposables.empty();
            cd.add(d);
            cd.delete(d);
        }
        return cd;
    }

    @Benchmark
    public Object fixedSyncWithExisting() throws Exception {
        int n = Runtime.getRuntime().availableProcessors();
        FixedCompositeDisposable cd = new FixedCompositeDisposable(n);
        for (int i = 0; i < n - 1; i++) {
            cd.add(Disposables.empty());
        }
        int c = calls;
        for (int i = 0; i < c; i++) {
            Disposable d = Disposables.empty();
            cd.add(d);
            cd.delete(d);
        }
        return cd;
    }

    @Benchmark
    public Object stripedSync() throws Exception {
        StripedCompositeDisposable cd = new StripedCompositeDisposable(Runtime.getRuntime().availableProcessors());
        int c = calls;
        for (int i = 0; i < c; i++) {
            Disposable d = Disposables.empty();
            cd.add(d);
            cd.delete(d);
        }
        return cd;
    }

    @Benchmark
    public Object stripedSyncWithExisting() throws Exception {
        int n = Runtime.getRuntime().availableProcessors();
        StripedCompositeDisposable cd = new StripedCompositeDisposable(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < n - 1; i++) {
            cd.add(Disposables.empty());
        }
        int c = calls;
        for (int i = 0; i < c; i++) {
            Disposable d = Disposables.empty();
            cd.add(d);
            cd.delete(d);
        }
        return cd;
    }

}
