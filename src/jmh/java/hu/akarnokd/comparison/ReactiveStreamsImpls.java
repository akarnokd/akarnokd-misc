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
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import com.typesafe.config.*;

import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.impl.fusing.Fusing;
import akka.stream.javadsl.*;
import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import reactor.core.publisher.Flux;
import rsc.publisher.Px;

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

    
    Flowable<Integer> rx2Range;
    Flowable<Integer> rx2RangeFlatMapJust;
    Flowable<Integer> rx2RangeFlatMapRange;
    Flowable<Integer> rx2RangeAsync;
    Flowable<Integer> rx2RangePipeline;

    Flux<Integer> raRange;
    Flux<Integer> raRangeFlatMapJust;
    Flux<Integer> raRangeFlatMapRange;
    Flux<Integer> raRangeAsync;
    Flux<Integer> raRangePipeline;

    Px<Integer> rscRange;
    Px<Integer> rscRangeFlatMapJust;
    Px<Integer> rscRangeFlatMapRange;
    Px<Integer> rscRangeAsync;
    Px<Integer> rscRangePipeline;

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

    reactor.core.scheduler.Scheduler singleRa1;
    reactor.core.scheduler.Scheduler singleRa2;
    
    @Setup
    public void setup() throws Exception {
        exec1 = Executors.newSingleThreadScheduledExecutor();
        exec2 = Executors.newSingleThreadScheduledExecutor();
        
        single1 = rx.schedulers.Schedulers.from(exec1);
        single2 = rx.schedulers.Schedulers.from(exec2);
        Scheduler single3 = Schedulers.single();
        Scheduler single4 = Schedulers.from(exec2);

        singleRa1 = reactor.core.scheduler.Schedulers.newSingle("A");
        singleRa2 = reactor.core.scheduler.Schedulers.newSingle("B");

        rxRange = rx.Observable.range(1, times);
        rxRangeFlatMapJust = rxRange.flatMap(rx.Observable::just);
        rxRangeFlatMapRange = rxRange.flatMap(v -> rx.Observable.range(v, 2));
        rxRangeAsync = rxRange.observeOn(single1);
        rxRangePipeline = rxRange.subscribeOn(single1).observeOn(single2);
        
        rx2Range = Flowable.range(1, times);
        rx2RangeFlatMapJust = rx2Range.flatMap(Flowable::just);
        rx2RangeFlatMapRange = rx2Range.flatMap(v -> Flowable.range(v, 2));
        rx2RangeAsync = rx2Range.observeOn(single3);
        rx2RangePipeline = rx2Range.subscribeOn(single3).observeOn(single4);

        raRange = Flux.range(1, times);
        raRangeFlatMapJust = raRange.flatMap(Flux::just);
        raRangeFlatMapRange = raRange.flatMap(v -> Flux.range(v, 2));
        raRangeAsync = raRange.publishOn(singleRa1);
        raRangePipeline = raRange.subscribeOn(singleRa1).publishOn(singleRa2);

        rscRange = Px.range(1, times);
        rscRangeFlatMapJust = rscRange.flatMap(Px::just);
        rscRangeFlatMapRange = rscRange.flatMap(v -> Px.range(v, 2));
        rscRangeAsync = rscRange.observeOn(exec1);
        rscRangePipeline = rscRange.subscribeOn(exec1).observeOn(exec2);

        values = rx2Range.toList().blockingFirst();
        

        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImpls.class, "/akka-streams.conf").resolve();
        actorSystem = ActorSystem.create("sys", cfg);

        materializer = ActorMaterializer.create(actorSystem);
        
        akRange = s -> {
            Source.range(1, times)
            .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
            .subscribe(s);
        };
        
        akRangeFlatMapJust = s -> 
                Source.range(1, times)
                .flatMapMerge(2, v -> Source.single(v))
                .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
                .subscribe(s)
                ;

        akRangeFlatMapRange = s -> {
            Source.from(values)
            .flatMapMerge(2, v -> Source.range(v, v + 1))
            .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
            .subscribe(s)
            ;
        };

        akRangeAsync = akRange;
        akRangePipeline = akRange;

        ak2Range = s -> {
            optimize(Source.range(1, times))
            .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
            .subscribe(s);
        };
        
        ak2RangeFlatMapJust = s -> 
                optimize(Source.range(1, times)
                .flatMapMerge(2, v -> Source.single(v)))
                .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
                .subscribe(s)
                ;

        ak2RangeFlatMapRange = s -> {
            optimize(Source.from(values)
            .flatMapMerge(2, v -> Source.range(v, v + 1)))
            .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
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
        actorSystem.terminate();

        singleRa1.shutdown();
        singleRa2.shutdown();

        exec1.shutdownNow();
        exec2.shutdownNow();
    }

    // -------------------------------------------------------------------------

//    @Benchmark
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
    
//    @Benchmark
    public void rangeAsync_rx(Blackhole bh) throws InterruptedException {
        LatchedObserver<Integer> lo = new LatchedObserver<>(bh);
        rxRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
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
    
//    @Benchmark
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
    
//    @Benchmark
    public void rangeAsync_rx2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        rx2RangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
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
    
//    @Benchmark
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
    
//    @Benchmark
    public void rangeAsync_rsc(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        rscRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
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
    
//    @Benchmark
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
    
//    @Benchmark
    public void rangeAsync_reactor(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Object> lo = new LatchedRSObserver<>(bh);
        raRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
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

//    @Benchmark
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

    
//    @Benchmark
    public void rangeAsync_akka(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        akRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }
    
//    @Benchmark
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
        
        List<Integer> values = Observable.range(1, 1000).toList().blockingFirst();
        System.out.println(values.size());
        try {
            Flowable.fromPublisher(o.ak2Range)
            .subscribe(System.out::println, 
                    Throwable::printStackTrace, 
                    () -> System.out.println("Done"), 
                s -> {
                    System.out.println(s.getClass() + " " + s);
                    s.request(Long.MAX_VALUE);
            });
    
            Thread.sleep(1000);
        } finally {
            o.teardown();
        }
    }

    // -------------------------------------------------------------------------

//    @Benchmark
    public void range_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2Range.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
    public void rangeFlatMapJust_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2RangeFlatMapJust.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

//    @Benchmark
    public void rangeFlatMapRange_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2RangeFlatMapRange.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }

    
//    @Benchmark
    public void rangeAsync_akka2(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        ak2RangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }
    
//    @Benchmark
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