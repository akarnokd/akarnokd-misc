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
import reactor.core.publisher.Flux;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class AkkaStreamsReactorCrossMapPerf {
    @Param({ "1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    ActorSystem actorSystem;

    ActorMaterializer materializer;

    Publisher<Integer> flatMapAkkaAkka;

    Publisher<Integer> concatMapAkkaAkka;

    Publisher<Integer> flatMapAkkaRx;

    Publisher<Integer> concatMapAkkaRx;
    
    Publisher<Integer> concatMapRxAkka;
    
    Publisher<Integer> flatMapRxAkka;
    
    Publisher<Integer> concatMapAkkaRxAkka;
    
    Publisher<Integer> flatMapAkkaRxAkka;

    Publisher<Integer> concatMapRxRx;
    
    Publisher<Integer> flatMapRxRx;

    @Setup
    public void setup() throws Exception {
        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImplsAsync.class, "/akka-streams.conf").resolve();
        actorSystem = ActorSystem.create("sys", cfg);

        materializer = ActorMaterializer.create(actorSystem);

        Integer[] outer = new Integer[count];
        Arrays.fill(outer, 777);
        List<Integer> outerIterable = Arrays.asList(outer);
        Flux<Integer> outerRx = Flux.fromArray(outer);
        
        Integer[] inner = new Integer[1_000_000 / count];
        Arrays.fill(inner, 888);
        List<Integer> innerIterable = Arrays.asList(inner);
        Flux<Integer> innerRx = Flux.fromArray(inner);
        
        flatMapAkkaAkka = s -> 
        Source.from(outerIterable)
        .flatMapMerge(4, v -> Source.from(innerIterable))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
        
        concatMapAkkaAkka = s -> 
        Source.from(outerIterable)
        .flatMapConcat(v -> Source.from(innerIterable))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
        
        flatMapAkkaRx = s -> 
        Source.from(outerIterable)
        .flatMapMerge(4, v -> Source.fromPublisher(innerRx))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
        
        concatMapAkkaRx = s -> 
        Source.from(outerIterable)
        .flatMapConcat(v -> Source.fromPublisher(innerRx))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
        
        concatMapAkkaRxAkka = s -> 
            Flux.from(
                    Source.from(outerIterable)
                    .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
            )
            .concatMap(v -> 
                    Source.from(innerIterable)
                    .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
            )
            .subscribe(s);
        
        flatMapAkkaRxAkka = s -> 
            Flux.from(
                    Source.from(outerIterable)
                    .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
            )
            .flatMap(v -> 
                    Source.from(innerIterable)
                    .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer), 4
            )
            .subscribe(s);

        concatMapRxAkka = outerRx.concatMap(v -> 
        Source.from(innerIterable).runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer));
        
        flatMapRxAkka = outerRx.flatMap(v -> 
        Source.from(innerIterable).runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer), 4);
        
        concatMapRxRx = outerRx.concatMap(v -> innerRx.subscribeOn(reactor.core.scheduler.Schedulers.parallel()));
        
        flatMapRxRx = outerRx.flatMap(v -> innerRx.subscribeOn(reactor.core.scheduler.Schedulers.parallel()), 4);
    }
    
    @TearDown
    public void teardown() {
        actorSystem.terminate();
    }
    
    @Benchmark
    public void flatMapAkkaAkka(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        concatMapAkkaAkka.subscribe(s);
        s.await(1_000_000, 120);
    }
    
    @Benchmark
    public void concatMapAkkaAkka(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        concatMapAkkaAkka.subscribe(s);
        s.await(1_000_000, 120);
    }
    
    @Benchmark
    public void flatMapAkkaRx(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        flatMapAkkaRx.subscribe(s);
        s.await(1_000_000, 120);
    }
    
    @Benchmark
    public void concatMapAkkaRx(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        concatMapAkkaRx.subscribe(s);
        s.await(1_000_000, 120);
    }
    
    @Benchmark
    public void concatMapRxAkka(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        concatMapRxAkka.subscribe(s);
        s.await(1_000_000, 120);
    }

    
    @Benchmark
    public void flatMapRxAkka(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        flatMapRxAkka.subscribe(s);
        s.await(1_000_000, 120);
    }
    
    @Benchmark
    public void concatMapAkkaRxAkka(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        concatMapAkkaRxAkka.subscribe(s);
        s.await(1_000_000, 120);
    }

    
    @Benchmark
    public void flatMapAkkaRxAkka(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        flatMapAkkaRxAkka.subscribe(s);
        s.await(1_000_000, 120);
    }
    
    @Benchmark
    public void concatMapRxRx(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        concatMapRxRx.subscribe(s);
        s.await(1_000_000, 120);
    }

    
    @Benchmark
    public void flatMapRxRx(Blackhole bh) {
        PerfAsyncConsumer s = new PerfAsyncConsumer(bh);
        flatMapRxRx.subscribe(s);
        s.await(1_000_000, 120);
    }

    public static void main(String[] args) {
        
        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImplsAsync.class, "/akka-streams.conf").resolve();
        ActorSystem actorSystem = ActorSystem.create("sys", cfg);

        ActorMaterializer materializer = ActorMaterializer.create(actorSystem);

        
        Integer[] outer = new Integer[100];
        Arrays.fill(outer, 777);
        List<Integer> outerIterable = Arrays.asList(outer);
        
        Integer[] inner = new Integer[100];
        Arrays.fill(inner, 888);
        List<Integer> innerIterable = Arrays.asList(inner);
        
        Flux.from(
                s -> 
                Flux.from(
                        Source.from(outerIterable)
                        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
                )
                .flatMap(v -> 
                        Source.from(innerIterable)
                        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
                ).subscribe(s)
        )
        .subscribe(System.out::println);
    }
}
