package hu.akarnokd.comparison;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import com.typesafe.config.*;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;
import hu.akarnokd.reactive.comparison.PerfAsyncConsumer;
import io.reactivex.Flowable;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class AkkaStreamsCrossMapPerf {
    @Param({ "1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    ActorSystem actorSystem;

    ActorMaterializer materializer;

    Publisher<Integer> akRangeFlatMapXRange;

    Publisher<Integer> akRangeConcatMapXRange;

    Publisher<Integer> akRangeFlatMapXRangeRx;

    Publisher<Integer> akRangeConcatMapXRangeRx;

    @Setup
    public void setup() throws Exception {
        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImplsAsync.class, "/akka-streams.conf").resolve();
        actorSystem = ActorSystem.create("sys", cfg);

        materializer = ActorMaterializer.create(actorSystem);

        Integer[] outer = new Integer[count];
        Arrays.fill(outer, 777);
        List<Integer> outerIterable = Arrays.asList(outer);
        
        Integer[] inner = new Integer[1_000_000 / count];
        Arrays.fill(inner, 888);
        List<Integer> innerIterable = Arrays.asList(inner);
        Flowable<Integer> innerRx = Flowable.fromArray(inner);
        
        akRangeFlatMapXRange = s -> 
        Source.from(outerIterable)
        .flatMapMerge(4, v -> Source.from(innerIterable))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
        
        akRangeConcatMapXRange = s -> 
        Source.from(outerIterable)
        .flatMapConcat(v -> Source.from(innerIterable))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
        
        akRangeFlatMapXRangeRx = s -> 
        Source.from(outerIterable)
        .flatMapMerge(4, v -> Source.fromPublisher(innerRx))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
        
        akRangeConcatMapXRangeRx = s -> 
        Source.from(outerIterable)
        .flatMapConcat(v -> Source.fromPublisher(innerRx))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
    }
    
    @TearDown
    public void teardown() {
        actorSystem.terminate();
    }
    
    @Benchmark
    public void flatMapAkka(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        akRangeFlatMapXRange.subscribe(s);
        s.await(1_000_000);
    }
    
    @Benchmark
    public void concatMapAkka(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        akRangeFlatMapXRange.subscribe(s);
        s.await(1_000_000);
    }
    
    @Benchmark
    public void flatMapAkkaRx(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        akRangeFlatMapXRangeRx.subscribe(s);
        s.await(1_000_000);
    }
    
    @Benchmark
    public void concatMapAkkaRx(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        akRangeFlatMapXRangeRx.subscribe(s);
        s.await(1_000_000);
    }
    
    public static void main(String[] args) {
        
        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImplsAsync.class, "/akka-streams.conf").resolve();
        ActorSystem actorSystem = ActorSystem.create("sys", cfg);

        ActorMaterializer materializer = ActorMaterializer.create(actorSystem);

        
        Integer[] outer = new Integer[1000];
        Arrays.fill(outer, 777);
        List<Integer> outerIterable = Arrays.asList(outer);
        
        Integer[] inner = new Integer[1_000_000 / 1000];
        Arrays.fill(inner, 888);
        List<Integer> innerIterable = Arrays.asList(inner);
        
        Flowable.fromPublisher(Source.from(outerIterable)
        .flatMapMerge(4, v -> Source.from(innerIterable))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        )
        .subscribe(System.out::println);
    }
}
