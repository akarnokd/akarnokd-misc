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

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import hu.akarnokd.rxjava2.NbpObservable;
import hu.akarnokd.rxjava2.Observable;
import hu.akarnokd.rxjava2.schedulers.Schedulers;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class StreamVsRxJava {
    @Param({ "1000", "10000", "100000" })
    public int times;

    List<String> rows;
    
    @Setup
    public void setup() {
        rows = new ArrayList<>();
        
        int n = times;
        
        Random rnd = new Random(0);
        
        for (int i = 0; i < n; i++) {
            StringBuilder b = new StringBuilder();
            
            int nwords = rnd.nextInt(60) + 10;
            
            for (int j = 0; j < nwords; j++) {
                if (j != 0) {
                    b.append(' ');
                }
                int nletters = rnd.nextInt(10) + 1;
                
                for (int k = 0; k < nletters; k++) {
                    b.append('a' + rnd.nextInt(26));
                }
            }
            
            rows.add(b.toString());
        }
    }
    
//    @Benchmark
    public void streamSerial() {
        rows.stream()
        .flatMap(r -> {
            String[] ws = r.split("\\s");
            return Arrays.asList(ws).stream();
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        ;
    }
    
//    @Benchmark
    public void streamParallel() {
        rows.parallelStream()
        .flatMap(r -> {
            String[] ws = r.split("\\s");
            return Arrays.asList(ws).stream();
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        ;
    }
    
//    @Benchmark
    public void rx2Serial() {
        Observable.fromIterable(rows)
        .flatMap(r -> {
            String[] ws = r.split("\\s");
            return Observable.fromArray(ws);
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .subscribe()
        ;
    }
    
//    @Benchmark
    public void rx2Parallel() {
        Observable.fromIterable(rows)
        .flatMap(r -> {
            String[] ws = r.split("\\s");
            return Observable.fromArray(ws)
                    .subscribeOn(Schedulers.computation());
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }
    
//    @Benchmark
    public void rx2Parallel2() {
        Observable.fromIterable(rows)
        .flatMap(r -> {
            return Observable.just(r)
                    .subscribeOn(Schedulers.computation())
                    .flatMap(rs -> Observable.fromArray(rs.split("\\s")));
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }
    
//    @Benchmark
    public void rx2oSerial() {
        NbpObservable.fromIterable(rows)
        .flatMap(r -> {
            String[] ws = r.split("\\s");
            return NbpObservable.fromArray(ws);
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .subscribe()
        ;
    }
    
//    @Benchmark
    public void rx2oParallel() {
        NbpObservable.fromIterable(rows)
        .flatMap(r -> {
            String[] ws = r.split("\\s");
            return NbpObservable.fromArray(ws)
                    .subscribeOn(Schedulers.computation());
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }
    
//    @Benchmark
    public void rx2oParallel2() {
        NbpObservable.fromIterable(rows)
        .flatMap(r -> {
            return NbpObservable.just(r)
                    .subscribeOn(Schedulers.computation())
                    .flatMap(rs -> NbpObservable.fromArray(rs.split("\\s")));
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }
    
//    @Benchmark
    public void rxSerial() {
        rx.Observable.from(rows)
        .flatMap(r -> {
            String[] ws = r.split("\\s");
            return rx.Observable.from(ws);
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .subscribe()
        ;
    }
    
//    @Benchmark
    public void rxParallel() {
        rx.Observable.from(rows)
        .flatMap(r -> {
            String[] ws = r.split("\\s");
            return rx.Observable.from(ws)
                    .subscribeOn(rx.schedulers.Schedulers.computation());
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }
    
//    @Benchmark
    public void rxParallel2() {
        rx.Observable.from(rows)
        .flatMap(r -> {
            return rx.Observable.just(r)
                    .subscribeOn(rx.schedulers.Schedulers.computation())
                    .flatMap(rs -> rx.Observable.from(rs.split("\\s")));
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }
    
    @Benchmark
    public void rxParallel3() {
        int[] k = new int[1];
        rx.Observable.from(rows)
        .groupBy(v -> k[0] & 7)
        .flatMap(g -> {
            return g.subscribeOn(rx.schedulers.Schedulers.computation())
            .flatMap(rs -> rx.Observable.from(rs.split("\\s")));
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }
    
    @Benchmark
    public void rx2Parallel3() {
        int[] k = new int[1];
        Observable.fromIterable(rows)
        .groupBy(v -> k[0] & 7)
        .flatMap(g -> {
            return g.subscribeOn(Schedulers.computation())
            .flatMap(rs -> Observable.fromArray(rs.split("\\s")));
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }

    @Benchmark
    public void rx2nParallel3() {
        int[] k = new int[1];
        NbpObservable.fromIterable(rows)
        .groupBy(v -> k[0] & 7)
        .flatMap(g -> {
            return g.subscribeOn(Schedulers.computation())
            .flatMap(rs -> NbpObservable.fromArray(rs.split("\\s")));
        })
        .filter(w -> w.length() > 4)
        .map(w -> w.length())
        .reduce(0, (a, b) -> a + b)
        .toBlocking()
        .last()
        ;
    }

}