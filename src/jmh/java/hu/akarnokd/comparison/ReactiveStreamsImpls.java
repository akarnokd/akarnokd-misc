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

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import com.typesafe.config.*;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;
import hu.akarnokd.rxjava2.Observable;
import hu.akarnokd.rxjava2.Scheduler;
import hu.akarnokd.rxjava2.schedulers.Schedulers;
import hu.akarnokd.rxjava2.subscribers.Subscribers;
import reactor.Processors;
import reactor.core.processor.ProcessorGroup;
import reactor.rx.*;
import scala.runtime.BoxedUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ReactiveStreamsImpls {
    @Param({ "1", "1000", "1000000" })
    public int times;
    
    rx.Observable<Integer> rxRange;
    rx.Observable<Integer> rxRangeFlatMapJust;
    rx.Observable<Integer> rxRangeFlatMapRange;
    rx.Observable<Integer> rxRangeAsync;
    rx.Observable<Integer> rxRangePipeline;

    
    hu.akarnokd.rxjava2.Observable<Integer> rx2Range;
    hu.akarnokd.rxjava2.Observable<Integer> rx2RangeFlatMapJust;
    hu.akarnokd.rxjava2.Observable<Integer> rx2RangeFlatMapRange;
    hu.akarnokd.rxjava2.Observable<Integer> rx2RangeAsync;
    hu.akarnokd.rxjava2.Observable<Integer> rx2RangePipeline;

    Stream<Integer> raRange;
    Stream<Integer> raRangeFlatMapJust;
    Stream<Integer> raRangeFlatMapRange;
    Stream<Integer> raRangeAsync;
    Stream<Integer> raRangePipeline;

//    Stream<Long> raRange;
//    Stream<Long> raRangeFlatMapJust;
//    Stream<Long> raRangeFlatMapRange;
//    Stream<Long> raRangeAsync;
//    Stream<Long> raRangePipeline;

    Publisher<Integer> akRange;
    Publisher<Integer> akRangeFlatMapJust;
    Publisher<Integer> akRangeFlatMapRange;
    Publisher<Integer> akRangeAsync;
    Publisher<Integer> akRangePipeline;
    
    
    rx.Scheduler single1;
    rx.Scheduler single2;
    
    ScheduledExecutorService exec1;
    ScheduledExecutorService exec2;
    
    ActorSystem actorSystem;

    ProcessorGroup<Object> g1;

    ProcessorGroup<Object> g2;

    private ActorMaterializer materializer;

    private List<Integer> values;
    
    @Setup
    public void setup() throws Exception {
        exec1 = Executors.newSingleThreadScheduledExecutor();
        exec2 = Executors.newSingleThreadScheduledExecutor();
        
        single1 = rx.schedulers.Schedulers.from(exec1);
        single2 = rx.schedulers.Schedulers.from(exec2);
        Scheduler single3 = Schedulers.single();
        Scheduler single4 = Schedulers.from(exec2);
        
        rxRange = rx.Observable.range(1, times);
        rxRangeFlatMapJust = rxRange.flatMap(rx.Observable::just);
        rxRangeFlatMapRange = rxRange.flatMap(v -> rx.Observable.range(v, 2));
        rxRangeAsync = rxRange.observeOn(single1);
        rxRangePipeline = rxRange.subscribeOn(single1).observeOn(single2);
        
        rx2Range = hu.akarnokd.rxjava2.Observable.range(1, times);
        rx2RangeFlatMapJust = rx2Range.flatMap(hu.akarnokd.rxjava2.Observable::just);
        rx2RangeFlatMapRange = rx2Range.flatMap(v -> hu.akarnokd.rxjava2.Observable.range(v, 2));
        rx2RangeAsync = rx2Range.observeOn(single3);
        rx2RangePipeline = rx2Range.subscribeOn(single3).observeOn(single4);

        g1 = Processors.asyncGroup("processor", 128, 1, null, null, false);
        g2 = Processors.asyncGroup("processor", 128, 1, null, null, false);
        
        raRange = Streams.range(1, times);
        raRangeFlatMapJust = raRange.flatMap(Streams::just);
        raRangeFlatMapRange = raRange.flatMap(v -> Streams.range(v, v + 1));
        raRangeAsync = raRange.dispatchOn(g1);
        raRangePipeline = raRange.publishOn(g1).dispatchOn(g2);
        
        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImpls.class, "/akka-streams.conf");
        actorSystem = ActorSystem.create("sys", cfg);

        materializer = ActorMaterializer.create(actorSystem);
        
        values = rx2Range.toList().toBlocking().first();
        
        akRange = s -> {
            Source.from(values)
            .runWith(Sink.publisher(), materializer)
            .subscribe(s);
        };
        
        Method m = akka.stream.javadsl.FlattenStrategy.class.getMethod("concat");
        @SuppressWarnings({ "rawtypes", "unchecked" })
        FlattenStrategy<Source<Integer, BoxedUnit>, Integer> flatten = (FlattenStrategy)m.invoke(null);
        
        akRangeFlatMapJust = s -> 
                Source.from(values)
                .map(v -> Source.single(v))
                .flatten(flatten)
                .runWith(Sink.publisher(), materializer)
                .subscribe(s)
                ;

        akRangeFlatMapRange = s -> {
            Source.from(values)
            .map(v -> Source.from(Arrays.asList(v, v + 1)))
            .flatten(flatten)
            .runWith(Sink.publisher(), materializer)
            .subscribe(s)
            ;
        };

        akRangeAsync = akRange;
        akRangePipeline = akRange;
    }
    
    @TearDown
    public void teardown() {
        actorSystem.shutdown();
        
        exec1.shutdownNow();
        exec2.shutdownNow();
        
        g1.shutdown();
        g2.shutdown();
    }

//    @Benchmark
    public void rxRange(Blackhole bh) {
        rxRange.subscribe(new LatchedObserver<Integer>(bh));
    }

//    @Benchmark
    public void rxRangeFlatMapJust(Blackhole bh) {
        rxRangeFlatMapJust.subscribe(new LatchedObserver<Integer>(bh));
    }
    
//    @Benchmark
    public void rxRangeFlatMapRange(Blackhole bh) {
        rxRangeFlatMapRange.subscribe(new LatchedObserver<Integer>(bh));
    }
    
//    @Benchmark
    public void rxRangeAsync(Blackhole bh) throws InterruptedException {
        LatchedObserver<Integer> lo = new LatchedObserver<>(bh);
        rxRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
    public void rxRangePipeline(Blackhole bh) throws InterruptedException {
        LatchedObserver<Integer> lo = new LatchedObserver<>(bh);
        rxRangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    // -------------------------------------------------------------------------
    
//    @Benchmark
    public void rx2Range(Blackhole bh) {
        rx2Range.subscribe(new LatchedRSObserver<Integer>(bh));
    }

//    @Benchmark
    public void rx2RangeFlatMapJust(Blackhole bh) {
        rx2RangeFlatMapJust.subscribe(new LatchedRSObserver<Integer>(bh));
    }
    
//    @Benchmark
    public void rx2RangeFlatMapRange(Blackhole bh) {
        rx2RangeFlatMapRange.subscribe(new LatchedRSObserver<Integer>(bh));
    }
    
//    @Benchmark
    public void rx2RangeAsync(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        rx2RangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
    public void rx2RangePipeline(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        rx2RangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    // -------------------------------------------------------------------------
    
//    @Benchmark
    public void raRange(Blackhole bh) {
        raRange.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void raRangeFlatMapJust(Blackhole bh) {
        raRangeFlatMapJust.subscribe(new LatchedRSObserver<>(bh));
    }
    
//    @Benchmark
    public void raRangeFlatMapRange(Blackhole bh) {
        raRangeFlatMapRange.subscribe(new LatchedRSObserver<>(bh));
    }
    
    @Benchmark
    public void raRangeAsync(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Object> lo = new LatchedRSObserver<>(bh);
        raRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void raRangePipeline(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Object> lo = new LatchedRSObserver<>(bh);
        raRangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    // -------------------------------------------------------------------------

//    @Benchmark
    public void akRange(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRange.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
    public void akRangeFlatMapJust(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRangeFlatMapJust.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
    public void akRangeFlatMapRange(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRangeFlatMapRange.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    
//    @Benchmark
    public void akRangeAsync(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }
    
//    @Benchmark
    public void akRangePipeline(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    public static void main(String[] args) throws Exception {
        ReactiveStreamsImpls o = new ReactiveStreamsImpls();
        
        o.times = 1000;
        o.setup();
        
        List<Integer> values = Observable.range(1, 1000).toList().toBlocking().first();
        System.out.println(values.size());
        try {
            o.akRange
            .subscribe(Subscribers.create(System.out::println, 
                    Throwable::printStackTrace, 
                    () -> System.out.println("Done"), 
                s -> {
                    System.out.println(s.getClass() + " " + s);
                    s.request(Long.MAX_VALUE);
            }));
    
            Thread.sleep(1000);
        } finally {
            o.teardown();
        }
    }
    
    // -------------------------------------------------------------------------
//@Benchmark
public void javaRange(Blackhole bh) {
    int n = times;
    for (int i = 0; i < n; i++) {
        bh.consume(i);
    }
}

//@Benchmark
public void javaRangeFlatMapJust(Blackhole bh) {
    int n = times;
    for (int i = 0; i < n; i++) {
        for (int j = i; j < i + 1; j++) {
            bh.consume(j);
        }
    }
}

//@Benchmark
public void javaRangeFlatMapRange(Blackhole bh) {
    int n = times;
    for (int i = 0; i < n; i++) {
        for (int j = i; j < i + 2; j++) {
            bh.consume(j);
        }
    }
}

//@Benchmark
public void streamRange(Blackhole bh) {
    values.stream().forEach(bh::consume);
}

//@Benchmark
public void streamRangeFlatMapJust(Blackhole bh) {
    values.stream()
    .flatMap(v -> Collections.singletonList(v).stream())
    .forEach(bh::consume);
}

//@Benchmark
public void streamRangeFlatMapRange(Blackhole bh) {
    values.stream()
    .flatMap(v -> Arrays.asList(v, v + 1).stream())
    .forEach(bh::consume);
}

//@Benchmark
public void pstreamRange(Blackhole bh) {
    values.parallelStream().forEach(bh::consume);
}

//@Benchmark
public void pstreamRangeFlatMapJust(Blackhole bh) {
    values.parallelStream()
    .flatMap(v -> Collections.singletonList(v).stream())
    .forEach(bh::consume);
}

//@Benchmark
public void pstreamRangeFlatMapRange(Blackhole bh) {
    values.parallelStream()
    .flatMap(v -> Arrays.asList(v, v + 1).stream())
    .forEach(bh::consume);
}
}