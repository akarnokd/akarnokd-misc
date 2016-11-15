package hu.akarnokd.comparison;

import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.*;

import com.typesafe.config.*;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class AkkaStreamsFlatMapPerf {
    @Param({ /*"1", "1000", */"1000000" })
    public int times;

    @Param({"1", "2", "16"})
    public int maxConcurrent;

    ActorSystem actorSystem;

    ActorMaterializer materializer;

    Publisher<Integer> akRangeFlatMapJust;

    Publisher<Integer> akRangeConcatMapJust;

    @Setup
    public void setup() throws Exception {
        Config cfg = ConfigFactory.parseResources(AkkaStreamsFlatMapPerf.class, "/akka-streams.conf");
        actorSystem = ActorSystem.create("sys", cfg);

        materializer = ActorMaterializer.create(actorSystem);

        akRangeFlatMapJust = s ->
        Source.range(1, times)
        .flatMapMerge(maxConcurrent, v -> Source.single(v))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;

        akRangeConcatMapJust = s ->
        Source.range(1, times)
        .flatMapConcat(v -> Source.single(v))
        .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer)
        .subscribe(s)
        ;
    }

    @TearDown
    public void teardown() {
        actorSystem.terminate();
    }

    @Benchmark
    public void rangeFlatMapJust_akka(Blackhole bh) throws InterruptedException {
        LatchedSubscriber<Integer> lo = new LatchedSubscriber<>(bh);
        akRangeFlatMapJust.subscribe(lo);

        if (times == 1) {
            while (lo.latch.getCount() != 0) {
                ;
            }
        } else {
            lo.latch.await();
        }
    }

    public class LatchedSubscriber<T> implements Subscriber<T> {

        public CountDownLatch latch = new CountDownLatch(1);
        private final Blackhole bh;

        public LatchedSubscriber(Blackhole bh) {
            this.bh = bh;
        }

        @Override
        public void onSubscribe(Subscription s) {
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onComplete() {
            latch.countDown();
        }

        @Override
        public void onError(Throwable e) {
            latch.countDown();
        }

        @Override
        public void onNext(T t) {
            bh.consume(t);
        }

    }

}
