package hu.akarnokd.reactive.comparison.rx1;

import java.lang.management.ManagementFactory;
import java.util.function.Supplier;

import rx.schedulers.Schedulers;

public final class MemoryPerf {

    private MemoryPerf() { }

    static long memoryUse() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    static final class MyRxSubscriber extends rx.Subscriber<Object> {
        MyRxSubscriber() {
            request(0);
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object t) {

        }
    }


    static <U> void checkMemory(Supplier<U> item, String name, String typeLib) throws Exception {
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

        System.out.println("Benchmark  (lib-type)   Mode  Cnt       Score       Error  Units");

        checkMemory(() -> rx.Observable.just(1), "just", "RxObservable");

        checkMemory(() -> rx.Observable.range(1, 10), "range", "RxObservable");

        checkMemory(() -> rx.Observable.empty(), "empty", "RxObservable");

        checkMemory(() -> rx.Observable.fromCallable(() -> 1), "fromCallable", "RxObservable");

        checkMemory(() -> new MyRxSubscriber(), "consumer", "RxObservable");

        checkMemory(() -> new rx.observers.TestSubscriber<>(), "test-consumer", "RxObservable");

        checkMemory(() -> rx.Observable.just(1).subscribe(new MyRxSubscriber()), "just+consumer", "RxObservable");

        checkMemory(() -> rx.Observable.range(1, 10).subscribe(new MyRxSubscriber()), "range+consumer", "RxObservable");

        checkMemory(() -> rx.Observable.range(1, 10).map(v -> v).subscribe(new MyRxSubscriber()),
                "range+map+consumer", "RxObservable");

        checkMemory(() -> rx.Observable.range(1, 10).map(v -> v).filter(v -> true).subscribe(new MyRxSubscriber()),
                "range+map+filter+consumer", "RxObservable");

        checkMemory(() -> rx.Observable.range(1, 10).subscribeOn(Schedulers.computation()).subscribe(new MyRxSubscriber()),
                "range+subscribeOn+consumer", "RxObservable");

        checkMemory(() -> rx.Observable.range(1, 10).observeOn(Schedulers.computation()).subscribe(new MyRxSubscriber()),
                "range+observeOn+consumer", "RxObservable");

        checkMemory(() -> rx.Observable.range(1, 10).subscribeOn(Schedulers.computation()).observeOn(Schedulers.computation()).subscribe(new MyRxSubscriber()),
                "range+subscribeOn+observeOn+consumer", "RxObservable");

        checkMemory(() -> rx.subjects.AsyncSubject.create(), "Async", "RxObservable");

        checkMemory(() -> rx.subjects.PublishSubject.create(), "Publish", "RxObservable");

        checkMemory(() -> rx.subjects.ReplaySubject.create(), "Replay", "RxObservable");

        checkMemory(() -> rx.subjects.BehaviorSubject.create(), "Behavior", "RxObservable");

        checkMemory(() -> rx.subjects.UnicastSubject.create(), "Unicast", "RxObservable");

        checkMemory(() -> rx.subjects.AsyncSubject.create().subscribe(new MyRxSubscriber()), "Async+consumer", "RxObservable");

        checkMemory(() -> rx.subjects.PublishSubject.create().subscribe(new MyRxSubscriber()), "Publish+consumer", "RxObservable");

        checkMemory(() -> rx.subjects.ReplaySubject.create().subscribe(new MyRxSubscriber()), "Replay+consumer", "RxObservable");

        checkMemory(() -> rx.subjects.BehaviorSubject.create().subscribe(new MyRxSubscriber()), "Behavior+consumer", "RxObservable");

        checkMemory(() -> rx.subjects.UnicastSubject.create().subscribe(new MyRxSubscriber()), "Unicast+consumer", "RxObservable");

        // ---------------------------------------------------------------------------------------------------------------------

    }
}
