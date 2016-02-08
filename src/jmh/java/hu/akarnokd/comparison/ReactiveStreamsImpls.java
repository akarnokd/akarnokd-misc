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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Graph;
import akka.stream.impl.fusing.Fusing;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import hu.akarnokd.rxjava2.Observable;
import hu.akarnokd.rxjava2.Scheduler;
import hu.akarnokd.rxjava2.schedulers.Schedulers;
import hu.akarnokd.rxjava2.subscribers.Subscribers;
import reactivestreams.commons.publisher.PublisherBase;
import reactor.core.publisher.SchedulerGroup;
import reactor.rx.Stream;

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

    PublisherBase<Integer> rscRange;
    PublisherBase<Integer> rscRangeFlatMapJust;
    PublisherBase<Integer> rscRangeFlatMapRange;
    PublisherBase<Integer> rscRangeAsync;
    PublisherBase<Integer> rscRangePipeline;

    Publisher<Integer> akRange;
    Publisher<Integer> akRangeFlatMapJust;
    Publisher<Integer> akRangeFlatMapRange;
    Publisher<Integer> akRangeAsync;
    Publisher<Integer> akRangePipeline;
    
    Publisher<Integer> ak2Range;
    Publisher<Integer> ak2RangeFlatMapJust;
    Publisher<Integer> ak2RangeFlatMapRange;
    Publisher<Integer> ak2RangeAsync;
    Publisher<Integer> ak2RangePipeline;
    
    rx.Scheduler single1;
    rx.Scheduler single2;
    
    ScheduledExecutorService exec1;
    ScheduledExecutorService exec2;
    
    ActorSystem actorSystem;

    private ActorMaterializer materializer;

    private List<Integer> values;

    SchedulerGroup singleRa1;
    SchedulerGroup singleRa2;
    
    @Setup
    public void setup() throws Exception {
        exec1 = Executors.newSingleThreadScheduledExecutor();
        exec2 = Executors.newSingleThreadScheduledExecutor();
        
        single1 = rx.schedulers.Schedulers.from(exec1);
        single2 = rx.schedulers.Schedulers.from(exec2);
        Scheduler single3 = Schedulers.single();
        Scheduler single4 = Schedulers.from(exec2);

        singleRa1 = SchedulerGroup.single("A");
        singleRa2 = SchedulerGroup.single("B");

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

        raRange = Stream.range(1, times);
        raRangeFlatMapJust = raRange.flatMap(Stream::just);
        raRangeFlatMapRange = raRange.flatMap(v -> Stream.range(v, 2));
        raRangeAsync = raRange.dispatchOn(singleRa1);
        raRangePipeline = raRange.publishOn(singleRa1).dispatchOn(singleRa2);

        rscRange = PublisherBase.range(1, times);
        rscRangeFlatMapJust = rscRange.flatMap(PublisherBase::just);
        rscRangeFlatMapRange = rscRange.flatMap(v -> PublisherBase.range(v, 2));
        rscRangeAsync = rscRange.observeOn(exec1);
        rscRangePipeline = rscRange.subscribeOn(exec1).observeOn(exec2);

        values = rx2Range.toList().toBlocking().first();
        

        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImpls.class, "/akka-streams.conf");
        actorSystem = ActorSystem.create("sys", cfg);

        materializer = ActorMaterializer.create(actorSystem);
        
        akRange = s -> {
            Source.range(1, times)
            .runWith(Sink.asPublisher(false), materializer)
            .subscribe(s);
        };
        
        akRangeFlatMapJust = s -> 
                Source.range(1, times)
                .flatMapMerge(2, v -> Source.single(v))
                .runWith(Sink.asPublisher(false), materializer)
                .subscribe(s)
                ;

        akRangeFlatMapRange = s -> {
            Source.from(values)
            .flatMapMerge(2, v -> Source.range(v, v + 1))
            .runWith(Sink.asPublisher(true), materializer)
            .subscribe(s)
            ;
        };

        akRangeAsync = akRange;
        akRangePipeline = akRange;

        ak2Range = s -> {
            optimize(Source.range(1, times))
            .runWith(Sink.asPublisher(false), materializer)
            .subscribe(s);
        };
        
        ak2RangeFlatMapJust = s -> 
                optimize(Source.range(1, times)
                .flatMapMerge(2, v -> Source.single(v)))
                .runWith(Sink.asPublisher(false), materializer)
                .subscribe(s)
                ;

        ak2RangeFlatMapRange = s -> {
            optimize(Source.from(values)
            .flatMapMerge(2, v -> Source.range(v, v + 1)))
            .runWith(Sink.asPublisher(true), materializer)
            .subscribe(s)
            ;
        };

        ak2RangeAsync = ak2Range;
        ak2RangePipeline = ak2Range;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <V> Source<Integer, V> optimize(Graph g) {
        return Source.fromGraph(Fusing.aggressive(g));
    }
    
    @TearDown
    public void teardown() {
        actorSystem.shutdown();

        singleRa1.shutdown();
        singleRa2.shutdown();

        exec1.shutdownNow();
        exec2.shutdownNow();
    }

    // -------------------------------------------------------------------------

    @Benchmark
    public void range_rx(Blackhole bh) {
        rxRange.subscribe(new LatchedObserver<Integer>(bh));
    }

    @Benchmark
    public void rangeFlatMapJust_rx(Blackhole bh) {
        rxRangeFlatMapJust.subscribe(new LatchedObserver<Integer>(bh));
    }
    
    @Benchmark
    public void rangeFlatMapRange_rx(Blackhole bh) {
        rxRangeFlatMapRange.subscribe(new LatchedObserver<Integer>(bh));
    }
    
    @Benchmark
    public void rangeAsync_rx(Blackhole bh) throws InterruptedException {
        LatchedObserver<Integer> lo = new LatchedObserver<>(bh);
        rxRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void rangePipeline_rx(Blackhole bh) throws InterruptedException {
        LatchedObserver<Integer> lo = new LatchedObserver<>(bh);
        rxRangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    // -------------------------------------------------------------------------
    
    @Benchmark
    public void range_rx2(Blackhole bh) {
        rx2Range.subscribe(new LatchedRSObserver<Integer>(bh));
    }

    @Benchmark
    public void rangeFlatMapJust_rx2(Blackhole bh) {
        rx2RangeFlatMapJust.subscribe(new LatchedRSObserver<Integer>(bh));
    }
    
    @Benchmark
    public void rangeFlatMapRange_rx2(Blackhole bh) {
        rx2RangeFlatMapRange.subscribe(new LatchedRSObserver<Integer>(bh));
    }
    
    @Benchmark
    public void rangeAsync_rx2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        rx2RangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void rangePipeline_rx2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        rx2RangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    // -------------------------------------------------------------------------
    
    @Benchmark
    public void range_rsc(Blackhole bh) {
        rscRange.subscribe(new LatchedRSObserver<Integer>(bh));
    }

    @Benchmark
    public void rangeFlatMapJust_rsc(Blackhole bh) {
        rscRangeFlatMapJust.subscribe(new LatchedRSObserver<Integer>(bh));
    }
    
    @Benchmark
    public void rangeFlatMapRange_rsc(Blackhole bh) {
        rscRangeFlatMapRange.subscribe(new LatchedRSObserver<Integer>(bh));
    }
    
    @Benchmark
    public void rangeAsync_rsc(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        rscRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void rangePipeline_rsc(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        rscRangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    // -------------------------------------------------------------------------
    
    @Benchmark
    public void range_reactor(Blackhole bh) {
        raRange.subscribe(new LatchedRSObserver<>(bh));
    }

    @Benchmark
    public void rangeFlatMapJust_reactor(Blackhole bh) {
        raRangeFlatMapJust.subscribe(new LatchedRSObserver<>(bh));
    }
    
    @Benchmark
    public void rangeFlatMapRange_reactor(Blackhole bh) {
        raRangeFlatMapRange.subscribe(new LatchedRSObserver<>(bh));
    }
    
    @Benchmark
    public void rangeAsync_reactor(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Object> lo = new LatchedRSObserver<>(bh);
        raRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void rangePipeline_reactor(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Object> lo = new LatchedRSObserver<>(bh);
        raRangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    // -------------------------------------------------------------------------

    @Benchmark
    public void range_akka(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRange.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void rangeFlatMapJust_akka(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRangeFlatMapJust.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void rangeFlatMapRange_akka(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRangeFlatMapRange.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    
    @Benchmark
    public void rangeAsync_akka(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }
    
    @Benchmark
    public void rangePipeline_akka(Blackhole bh) throws InterruptedException {
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
            o.ak2Range
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

    @Benchmark
    public void range_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2Range.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void rangeFlatMapJust_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2RangeFlatMapJust.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    @Benchmark
    public void rangeFlatMapRange_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2RangeFlatMapRange.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    
    @Benchmark
    public void rangeAsync_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2RangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }
    
    @Benchmark
    public void rangePipeline_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2RangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
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