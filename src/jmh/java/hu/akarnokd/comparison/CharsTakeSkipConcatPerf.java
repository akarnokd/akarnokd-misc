/*
 * Copyright 2017 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.akarnokd.comparison;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import hu.akarnokd.reactive.comparison.consumers.PerfConsumer;
import hu.akarnokd.rxjava2.string.StringFlowable;
import io.reactivex.Flowable;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh="CharsTakeSkipConcatPerf"
 */
@BenchmarkMode(Mode.SampleTime)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class CharsTakeSkipConcatPerf {

    @Benchmark
    public void chars(Blackhole bh) {
        StringFlowable.characters("jezebel").subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void take(Blackhole bh) {
        StringFlowable.characters("jezebel").take(3).subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    public void skip(Blackhole bh) {
        StringFlowable.characters("jezebel").skip(3).subscribe(new PerfConsumer(bh));
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void concat(Blackhole bh) {
        Flowable.concatArray(StringFlowable.characters("jezebel").take(3),
                StringFlowable.characters("jezebel").skip(3))
                .subscribe(new PerfConsumer(bh));
    }

}