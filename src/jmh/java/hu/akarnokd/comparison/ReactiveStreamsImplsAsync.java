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
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;
import hu.akarnokd.akka.ActorScheduler;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import reactor.core.publisher.Flux;
import rsc.publisher.Px;
import rsc.scheduler.SingleScheduler;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ReactiveStreamsImplsAsync {
    @Param({ "1", "1000", "1000000" })
    public int times;
    
    rx.Observable<Integer> rxRangeAsync;
    rx.Observable<Integer> rxRangePipeline;

    
    Flowable<Integer> rx2RangeAsync;
    Flowable<Integer> rx2RangePipeline;

    Flux<Integer> raRangeAsync;
    Flux<Integer> raRangePipeline;

    Px<Integer> rscRangeAsync;
    Px<Integer> rscRangePipeline;

    Publisher<Integer> akRangeAsync;
    Publisher<Integer> akRangePipeline;
    
    Publisher<Integer> asRangeAsync;
    Publisher<Integer> asRangePipeline;

    rx.Scheduler single1;
    rx.Scheduler single2;
    
    ScheduledExecutorService exec1;
    ScheduledExecutorService exec2;
    
    ActorSystem actorSystem;

    private ActorMaterializer materializer;

    reactor.core.scheduler.Scheduler singleRa1;
    reactor.core.scheduler.Scheduler singleRa2;
    
    @Setup
    public void setup() throws Exception {
        Integer[] array = new Integer[times];
        for (int i = 0; i < times; i++) {
            array[i] = i + 1;
        }
        

        exec1 = Executors.newSingleThreadScheduledExecutor();
        exec2 = Executors.newSingleThreadScheduledExecutor();
        
        single1 = rx.schedulers.Schedulers.from(exec1);
        single2 = rx.schedulers.Schedulers.from(exec2);
        io.reactivex.Scheduler single3 = Schedulers.single();
        io.reactivex.Scheduler single4 = new io.reactivex.internal.schedulers.SingleScheduler();

        singleRa1 = reactor.core.scheduler.Schedulers.newSingle("A");
        singleRa2 = reactor.core.scheduler.Schedulers.newSingle("B");

        rx.Observable<Integer> rxRange = rx.Observable.from(array);
        rxRangeAsync = rxRange.observeOn(single1);
        rxRangePipeline = rxRange.subscribeOn(single1).observeOn(single2);
        
        Flowable<Integer> rx2Range = Flowable.fromArray(array);
        rx2RangeAsync = rx2Range.observeOn(single3);
        rx2RangePipeline = rx2Range.subscribeOn(single3).observeOn(single4);

        Flux<Integer> raRange = Flux.fromArray(array);
        raRangeAsync = raRange.publishOn(singleRa1);
        raRangePipeline = raRange.subscribeOn(singleRa1).publishOn(singleRa2);

        SingleScheduler rscs1 = new SingleScheduler("Rscs1", true);
        SingleScheduler rscs2 = new SingleScheduler("Rscs2", true);
        
        Px<Integer> rscRange = Px.fromArray(array);
        rscRangeAsync = rscRange.observeOn(rscs1);
        rscRangePipeline = rscRange.subscribeOn(rscs1).observeOn(rscs2);

        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImplsAsync.class, "/akka-streams.conf").resolve();

        actorSystem = ActorSystem.create("sys", cfg);

        materializer = ActorMaterializer.create(actorSystem);
        
        List<Integer> list = Arrays.asList(array);
        Publisher<Integer> akRange = s -> {
            Source.fromIterator(() -> list.iterator())
            .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
            .subscribe(s);
        };
        
        akRangeAsync = akRange;
        akRangePipeline = akRange;

        ActorScheduler as1 = new ActorScheduler(actorSystem);
        ActorScheduler as2 = new ActorScheduler(actorSystem);
        
        asRangeAsync = raRange.publishOn(as1);
        asRangePipeline = raRange.subscribeOn(as1).publishOn(as2);
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

    // -------------------------------------------------------------------------
    
    @Benchmark
    public void rangeAsync_actorscheduler(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        asRangeAsync.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }
    
    @Benchmark
    public void rangePipeline_actorscheduler(Blackhole bh) throws InterruptedException {
        LatchedRSObserver<Integer> lo = new LatchedRSObserver<>(bh);
        asRangePipeline.subscribe(lo);
        
        if (times == 1) {
            while (lo.latch.getCount() != 0);
        } else {
            lo.latch.await();
        }
    }
    
    public static void main(String[] args) throws Exception {
        ReactiveStreamsImplsAsync o = new ReactiveStreamsImplsAsync();
        
        o.times = 1000;
        o.setup();
        
        try {
            Flowable.fromPublisher(o.asRangeAsync)
            .subscribe(System.out::println, 
                    Throwable::printStackTrace, 
                    () -> System.out.println("Done"), 
                s -> {
                    System.out.println(s.getClass() + " " + s);
                    s.request(Long.MAX_VALUE);
            });
    
            Thread.sleep(5000);
        } finally {
            o.teardown();
        }
    }


}