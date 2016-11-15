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

package hu.akarnokd.comparison;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import io.reactivex.*;
import reactor.core.publisher.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class OnesTypesFlatMapPerf {

    @Param({"1", "1000", "1000000"})
    public int count;

    Flowable<Integer> flowableMaybe;

    Flowable<Integer> mergeMaybe;

    Observable<Integer> observableMaybe;

    Flux<Integer> fluxMono;

    Flowable<Integer> flowableSingle;

    Observable<Integer> observableSingle;

    @Setup
    public void setup() throws Exception {
        Maybe<Integer> maybeOne = Maybe.just(1);
        Integer[] array = new Integer[count];
        Arrays.fill(array, 777);

        flowableMaybe = Flowable.fromArray(array).flatMap(v -> maybeOne.toFlowable());
        observableMaybe = Observable.fromArray(array).flatMap(v -> maybeOne.toObservable());

        Single<Integer> singleOne = Single.just(1);

        flowableSingle = Flowable.fromArray(array).flatMap(v -> singleOne.toFlowable());
        observableSingle = Observable.fromArray(array).flatMap(v -> singleOne.toObservable());

        Mono<Integer> monoOne = Mono.just(1);

        fluxMono = Flux.fromArray(array).flatMap(v -> monoOne);

        @SuppressWarnings("unchecked")
        Maybe<Integer>[] maybes = new Maybe[count];
        Arrays.fill(maybes, maybeOne);

        mergeMaybe = Maybe.mergeArray(maybes);
    }

    @Benchmark
    public Object flowableMaybe() {
        return flowableMaybe.blockingLast();
    }

    @Benchmark
    public Object mergeMaybe() {
        return mergeMaybe.blockingLast();
    }

    @Benchmark
    public Object observableMaybe() {
        return observableMaybe.blockingLast();
    }

    @Benchmark
    public Object flowableSingle() {
        return flowableSingle.blockingLast();
    }

    @Benchmark
    public Object observableSingle() {
        return observableSingle.blockingLast();
    }

    @Benchmark
    public Object fluxMono() {
        return fluxMono.blockLast();
    }

}