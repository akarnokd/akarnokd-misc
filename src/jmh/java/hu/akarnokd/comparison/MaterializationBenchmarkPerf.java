package hu.akarnokd.comparison;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import com.typesafe.config.*;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;
import akka.stream.scaladsl.Sink;
import hu.akarnokd.akka.ActorScheduler2;
import hu.akarnokd.reactive.comparison.consumers.PerfAsyncConsumer;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MaterializationBenchmarkPerf {

    @Param({"1", "10"})
    int complexity;

    RunnableGraph<NotUsed> akkaMap;

    Flowable<Integer> flowableMap;

    Flowable<Integer> flowable;

    Flowable<Integer> flowableMapAkkaScheduler;

    Flowable<Integer> flowableMapSync;

    Flowable<Integer> flowableMap2;

    ActorSystem actorSystem;

    ActorMaterializer materializer;

    @Setup
    public void setup() {
        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImplsAsync.class, "/akka-streams.conf").resolve();
        actorSystem = ActorSystem.create("sys", cfg);

        materializer = ActorMaterializer.create(actorSystem);

        Source<Integer, NotUsed> src = Source.single(1);

        for (int i = 0; i < complexity; i++) {
            src = src.map(v -> v);
        }
        akkaMap = src.to((Sink)Sink.ignore());

        Flowable<Integer> f = Flowable.just(1).subscribeOn(Schedulers.computation());

        for (int i = 0; i < complexity; i++) {
            f = f.map(v -> v).observeOn(Schedulers.computation(), true, 1);
        }


        flowableMap = f;

        Flowable<Integer> f2 = Flowable.just(1).subscribeOn(Schedulers.computation());

        for (int i = 0; i < complexity; i++) {
            f2 = f2.map(v -> v);
        }


        flowableMap2 = f2;

        Flowable<Integer> g = Flowable.just(1);

        for (int i = 0; i < complexity; i++) {
            g = g.map(v -> v);
        }

        flowableMapSync = g;

        ActorScheduler2 as = new ActorScheduler2(actorSystem, true);

        Flowable<Integer> h = Flowable.just(1).subscribeOn(as);
        for (int i = 0; i < complexity; i++) {
            h = h.map(v -> v).observeOn(as, true, 1);
        }


        flowableMapAkkaScheduler = h;

        Flowable<Integer> k = Flowable.just(1).subscribeOn(Schedulers.computation());

        for (int i = 0; i < complexity; i++) {
            k = k.observeOn(Schedulers.computation(), true, 1);
        }


        flowable = k;
    }

    @TearDown
    public void teardown() {
        actorSystem.terminate();
    }

    @Benchmark
    public Object akkaMap() {
        return akkaMap.run(materializer);
    }

    @Benchmark
    public Object flowableMap() {
        return flowableMap.blockingLast();
    }

    @Benchmark
    public Object flowableMap2(Blackhole bh) {
        PerfAsyncConsumer pc = new PerfAsyncConsumer(bh);
        flowableMap2.subscribe(pc);
        pc.await(1);
        return pc;
    }

    @Benchmark
    public Object flowable() {
        return flowable.blockingLast();
    }

    @Benchmark
    public Object flowableMapAkkaScheduler() {
        return flowableMapAkkaScheduler.blockingLast();
    }

    @Benchmark
    public Object flowableMapSync() {
        return flowableMapSync.blockingLast();
    }

    public static void main(String[] args) {
        MaterializationBenchmarkPerf bench = new MaterializationBenchmarkPerf();
        bench.complexity = 10;
        bench.setup();

        for (int i = 0; i < 100000; i++) {
            bench.flowableMapAkkaScheduler();
        }

        Source<Integer, NotUsed> src = Source.single(1);

        for (int i = 0; i < 10; i++) {
            src = src.map(v -> {
                System.out.println(Thread.currentThread());
                return v;
            });
        }

        src.to((Sink)Sink.ignore()).run(bench.materializer);

        bench.teardown();
    }
}
