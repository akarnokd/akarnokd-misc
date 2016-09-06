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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openjdk.jmh.annotations.*;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import reactor.core.publisher.Mono;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ZeroOnePerf {

    Mono<Integer> monoOne;
    
    Mono<Integer> monoEmpty;
    
    Single<Integer> single;
    
    Maybe<Integer> maybeOne;
    
    Maybe<Integer> maybeEmpty;
    
    rx.Single<Integer> singleOld;
    
    @Setup
    public void setup() throws Exception {
        
        monoOne = Mono.just(1);
        
        monoEmpty = Mono.empty();
        
        single = Single.just(1);
        
        maybeOne = Maybe.just(1);
        
        maybeEmpty = Maybe.empty();
        
        singleOld = rx.Single.just(1);
    }
    
    static final class WeakGet<T> extends AtomicReference<T> implements MaybeObserver<T> {

        /** */
        private static final long serialVersionUID = 8764248587644450181L;

        @Override
        public void onSubscribe(Disposable d) {
            
        }

        @Override
        public void onSuccess(T value) {
            lazySet(value);
        }

        @Override
        public void onError(Throwable e) {
            
        }

        @Override
        public void onComplete() {
            
        }
        
    }

    static <T> T blockingGet(Maybe<T> m) {
        WeakGet<T> g = new WeakGet<>();
        m.subscribe(g);
        return g.get();
    }
    
    @Benchmark
    public Object monoOne() {
        return monoOne.block();
    }
    
    @Benchmark
    public Object monoEmpty() {
        return monoEmpty.block();
    }
    
    @Benchmark
    public Object single() {
        return single.blockingGet();
    }
    
    @Benchmark
    public Object maybeOne() {
        return blockingGet(maybeOne);
    }
    
    @Benchmark
    public Object maybeEmpty() {
        return blockingGet(maybeEmpty);
    }

    @Benchmark
    public Object singleOld() {
        return singleOld.toBlocking().value();
    }
}