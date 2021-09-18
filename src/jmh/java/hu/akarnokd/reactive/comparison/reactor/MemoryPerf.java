package hu.akarnokd.reactive.comparison.reactor;

import java.lang.management.ManagementFactory;
import java.util.function.Supplier;

import org.reactivestreams.Subscription;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.*;

public final class MemoryPerf {

    private MemoryPerf() { }

    static long memoryUse() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    static final class MyReactorSubscriber implements CoreSubscriber<Object> {

        org.reactivestreams.Subscription s;

        @Override
        public void onSubscribe(Subscription s) {
            this.s = s;
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object t) {

        }
    }

    static <U> void checkMemory(Supplier<U> item, String name, String typeLib) throws Exception {

        // make sure classes are initialized
        item.get();

        int n = 1_000_000;
        Object[] array = new Object[n];

        System.gc();
        Thread.sleep(200);

        long before = memoryUse();

        for (int i = 0; i < n; i++) {
            array[i] = item.get();
        }

        long after = memoryUse();

        double use = (after - before) / 1024.0 / 1024.0;

        System.out.print(name);
        System.out.print(" ");
        System.out.print(typeLib);
        System.out.print("     thrpt 1000000           ");
        System.out.printf("%.3f  0.000 MB%n", use);

        System.gc();
        Thread.sleep(200);
    }

    public static void main(String[] args) throws Exception {

        Flux.just(1);

        System.out.println("Benchmark  (lib-type)   Mode  Cnt       Score       Error  Units");

        // ---------------------------------------------------------------------------------------------------------------------

        checkMemory(() -> Flux.just(1), "just", "ReactorFlux");

        checkMemory(() -> Flux.range(1, 10), "range", "ReactorFlux");

        checkMemory(() -> Flux.empty(), "empty", "ReactorFlux");

        checkMemory(() -> Mono.fromCallable(() -> 1), "fromCallable", "ReactorFlux");

        checkMemory(() -> new MyReactorSubscriber(), "consumer", "ReactorFlux");

        checkMemory(() -> new io.reactivex.observers.TestObserver<>(), "test-consumer", "ReactorFlux");

        checkMemory(() -> Flux.just(1).subscribeWith(new MyReactorSubscriber()),
                "just+consumer", "ReactorFlux");

        checkMemory(() -> Flux.range(1, 10).subscribeWith(new MyReactorSubscriber()),
                "range+consumer", "ReactorFlux");

        checkMemory(() -> Flux.range(1, 10).map(v -> v).subscribeWith(new MyReactorSubscriber()),
                "range+map+consumer", "ReactorFlux");

        checkMemory(() -> Flux.range(1, 10).map(v -> v).filter(v -> true).subscribeWith(new MyReactorSubscriber()),
                "range+map+filter+consumer", "ReactorFlux");

        checkMemory(() -> Flux.range(1, 10).subscribeOn(reactor.core.scheduler.Schedulers.parallel()).subscribeWith(new MyReactorSubscriber()),
                "range+subscribeOn+consumer", "ReactorFlux");

        checkMemory(() -> Flux.range(1, 10).publishOn(reactor.core.scheduler.Schedulers.parallel()).subscribeWith(new MyReactorSubscriber()),
                "range+observeOn+consumer", "ReactorFlux");

        checkMemory(() -> Flux.range(1, 10).subscribeOn(reactor.core.scheduler.Schedulers.parallel()).publishOn(reactor.core.scheduler.Schedulers.parallel()).subscribeWith(new MyReactorSubscriber()),
                "range+subscribeOn+observeOn+consumer", "ReactorFlux");

        checkMemory(() -> Sinks.many().multicast().directBestEffort(), "Publish", "ReactorFlux");

        checkMemory(() -> Sinks.many().replay(), "Replay", "ReactorFlux");

        checkMemory(() -> Sinks.many().unicast(), "Unicast", "ReactorFlux");

        checkMemory(() -> Sinks.many().multicast().directBestEffort().asFlux().subscribeWith(new MyReactorSubscriber()),
                "Publish+consumer", "ReactorFlux");

        checkMemory(() -> Sinks.many().replay().all().asFlux().subscribeWith(new MyReactorSubscriber()),
                "Replay+consumer", "ReactorFlux");

        checkMemory(() -> Sinks.many().unicast().onBackpressureBuffer().asFlux().subscribeWith(new MyReactorSubscriber()),
                "Unicast+consumer", "ReactorFlux");

        // ---------------------------------------------------------------------------------------------------------------------

    }
}
