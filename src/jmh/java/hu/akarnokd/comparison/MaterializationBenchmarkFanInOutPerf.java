package hu.akarnokd.comparison;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import com.typesafe.config.*;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;
import akka.stream.scaladsl.Broadcast;
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
public class MaterializationBenchmarkFanInOutPerf {

    @Param({"1", "10"})
    int complexity;

    RunnableGraph<NotUsed> akka;

    Flowable<Integer> rxSync;

    Flowable<Integer> rxSubscribeOn;

    Flowable<Integer> rxObserveOn;

    Flowable<Integer> rxSubscribeOnMergeArray;

    Flowable<Integer> rxObserveOnMergeArray;

    ActorSystem actorSystem;

    Materializer materializer;

    @Setup
    public void setup() {
        Config cfg = ConfigFactory.parseResources(getClass(), "/akka-streams.conf").resolve();
        actorSystem = ActorSystem.create("sys", cfg);

        materializer = Materializer.createMaterializer(actorSystem);

        akka = RunnableGraph.fromGraph(GraphDSL.create(b -> {
            Shape broadcast = b.add(new Broadcast(complexity, false));
            Outlet outlet = broadcast.getOutlets().get(0);

            for (int i = 1; i < complexity; i++) {
                UniformFanInShape merge = b.add(Merge.create(2));

                // outlet ~> merge

                b.from(outlet).toInlet(merge.in(0));

                // broadcast.out(i) ~> merge

                b.from(broadcast.getOutlets().get(i)).toInlet(merge.in(1));

                outlet = merge.out();
            }

            // Source.single(()) ~> broadcast

            b.to(broadcast.getInlets().get(0)).from((SourceShape)b.add(Source.single(1)));

            // outlet ~> Sink.ignore

            b.to(b.add(Sink.ignore())).fromOutlet(outlet);

            return ClosedShape$.MODULE$.getInstance();
        }));

        rxSync = Flowable.just(1)
        .publish(f -> {
            Flowable<Integer> g = f;

            for (int i = 1; i < complexity; i++) {
                g = g.mergeWith(f);
            }

            return g;
        });

        rxSubscribeOn = Flowable.just(1)
        .subscribeOn(Schedulers.computation())
        .publish(f -> {
            Flowable<Integer> g = f;

            for (int i = 1; i < complexity; i++) {
                g = g.mergeWith(f);
            }

            return g;
        });

        rxObserveOn = Flowable.just(1)
        .observeOn(Schedulers.computation())
        .publish(f -> {
            Flowable<Integer> g = f;

            for (int i = 1; i < complexity; i++) {
                g = g.mergeWith(f);
            }

            return g;
        });

        rxSubscribeOnMergeArray = Flowable.just(1)
        .subscribeOn(Schedulers.computation())
        .publish(f -> {
            if (complexity == 1) {
                return f;
            }

            Flowable<Integer>[] array = new Flowable[complexity];
            Arrays.fill(array, f);
            return Flowable.mergeArray(array);
        });

        rxObserveOnMergeArray = Flowable.just(1)
        .observeOn(Schedulers.computation())
        .publish(f -> {
            if (complexity == 1) {
                return f;
            }

            Flowable<Integer>[] array = new Flowable[complexity];
            Arrays.fill(array, f);
            return Flowable.mergeArray(array);
        });
    }

    @TearDown
    public void teardown() {
        actorSystem.terminate();
    }

    @Benchmark
    public Object akka() {
        return akka.run(materializer);
    }

    @Benchmark
    public Object rx(Blackhole bh) {
        PerfAsyncConsumer pc = new PerfAsyncConsumer(bh);
        rxSync.subscribe(pc);
        pc.await(1);
        return pc;
    }

    @Benchmark
    public Object rxSubscribeOn(Blackhole bh) {
        PerfAsyncConsumer pc = new PerfAsyncConsumer(bh);
        rxSubscribeOn.subscribe(pc);
        pc.await(1);
        return pc;
    }

    @Benchmark
    public Object rxObserveOn(Blackhole bh) {
        PerfAsyncConsumer pc = new PerfAsyncConsumer(bh);
        rxObserveOn.subscribe(pc);
        pc.await(1);
        return pc;
    }

    @Benchmark
    public Object rxSubscribeOnMerge(Blackhole bh) {
        PerfAsyncConsumer pc = new PerfAsyncConsumer(bh);
        rxSubscribeOnMergeArray.subscribe(pc);
        pc.await(1);
        return pc;
    }

    @Benchmark
    public Object rxObserveOnMerge(Blackhole bh) {
        PerfAsyncConsumer pc = new PerfAsyncConsumer(bh);
        rxObserveOnMergeArray.subscribe(pc);
        pc.await(1);
        return pc;
    }

    public static void main(String[] args) {
        MaterializationBenchmarkFanInOutPerf bench = new MaterializationBenchmarkFanInOutPerf();
        bench.complexity = 10;
        bench.setup();
        try {

            for (int i = 0; i < 100000; i++) {
                bench.akka();
            }
        } finally {
            bench.teardown();
        }
    }
}
