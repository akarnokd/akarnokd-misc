package hu.akarnokd.reactive.comparison.rx2;

import java.lang.management.ManagementFactory;
import java.util.function.Supplier;

import org.reactivestreams.Subscription;

import io.reactivex.FlowableSubscriber;
import io.reactivex.disposables.Disposable;

public final class MemoryPerf {

    private MemoryPerf() { }

    static long memoryUse() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    static final class MyRx2Subscriber implements FlowableSubscriber<Object> {

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

    static final class MyRx2Observer implements io.reactivex.Observer<Object>, io.reactivex.SingleObserver<Object>,
    io.reactivex.MaybeObserver<Object>, io.reactivex.CompletableObserver {

        Disposable s;

        @Override
        public void onSubscribe(Disposable s) {
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

        @Override
        public void onSuccess(Object value) {

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

        // ---------------------------------------------------------------------------------------------------------------------

        checkMemory(() -> io.reactivex.Observable.just(1), "just", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.range(1, 10), "range", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.empty(), "empty", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.fromCallable(() -> 1), "fromCallable", "Rx2Observable");

        checkMemory(() -> new MyRx2Observer(), "consumer", "Rx2Observable");

        checkMemory(() -> new io.reactivex.observers.TestObserver<>(), "test-consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.just(1).subscribeWith(new MyRx2Observer()),
                "just+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.range(1, 10).subscribeWith(new MyRx2Observer()),
                "range+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.range(1, 10).map(v -> v).subscribeWith(new MyRx2Observer()),
                "range+map+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.range(1, 10).map(v -> v).filter(v -> true).subscribeWith(new MyRx2Observer()),
                "range+map+filter+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.range(1, 10).subscribeOn(io.reactivex.schedulers.Schedulers.computation()).subscribeWith(new MyRx2Observer()),
                "range+subscribeOn+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.range(1, 10).observeOn(io.reactivex.schedulers.Schedulers.computation()).subscribeWith(new MyRx2Observer()),
                "range+observeOn+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.Observable.range(1, 10).subscribeOn(io.reactivex.schedulers.Schedulers.computation()).observeOn(io.reactivex.schedulers.Schedulers.computation()).subscribeWith(new MyRx2Observer()),
                "range+subscribeOn+observeOn+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.AsyncSubject.create(), "Async", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.PublishSubject.create(), "Publish", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.ReplaySubject.create(), "Replay", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.BehaviorSubject.create(), "Behavior", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.UnicastSubject.create(), "Unicast", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.AsyncSubject.create().subscribeWith(new MyRx2Observer()),
                "Async+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.PublishSubject.create().subscribeWith(new MyRx2Observer()),
                "Publish+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.ReplaySubject.create().subscribeWith(new MyRx2Observer()),
                "Replay+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.BehaviorSubject.create().subscribeWith(new MyRx2Observer()),
                "Behavior+consumer", "Rx2Observable");

        checkMemory(() -> io.reactivex.subjects.UnicastSubject.create().subscribeWith(new MyRx2Observer()),
                "Unicast+consumer", "Rx2Observable");

        // ---------------------------------------------------------------------------------------------------------------------

        checkMemory(() -> io.reactivex.Flowable.just(1), "just", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.range(1, 10), "range", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.empty(), "empty", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.fromCallable(() -> 1), "fromCallable", "Rx2Flowable");

        checkMemory(() -> new MyRx2Subscriber(), "consumer", "Rx2Flowable");

        checkMemory(() -> new io.reactivex.observers.TestObserver<>(), "test-consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.just(1).subscribeWith(new MyRx2Subscriber()),
                "just+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.range(1, 10).subscribeWith(new MyRx2Subscriber()),
                "range+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.range(1, 10).map(v -> v).subscribeWith(new MyRx2Subscriber()),
                "range+map+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.range(1, 10).map(v -> v).filter(v -> true).subscribeWith(new MyRx2Subscriber()),
                "range+map+filter+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.range(1, 10).subscribeOn(io.reactivex.schedulers.Schedulers.computation()).subscribeWith(new MyRx2Subscriber()),
                "range+subscribeOn+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.range(1, 10).observeOn(io.reactivex.schedulers.Schedulers.computation()).subscribeWith(new MyRx2Subscriber()),
                "range+observeOn+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.Flowable.range(1, 10).subscribeOn(io.reactivex.schedulers.Schedulers.computation()).observeOn(io.reactivex.schedulers.Schedulers.computation()).subscribeWith(new MyRx2Subscriber()),
                "range+subscribeOn+observeOn+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.AsyncProcessor.create(), "Async", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.PublishProcessor.create(), "Publish", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.ReplayProcessor.create(), "Replay", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.BehaviorProcessor.create(), "Behavior", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.UnicastProcessor.create(), "Unicast", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.AsyncProcessor.create().subscribeWith(new MyRx2Subscriber()),
                "Async+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.PublishProcessor.create().subscribeWith(new MyRx2Subscriber()),
                "Publish+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.ReplayProcessor.create().subscribeWith(new MyRx2Subscriber()),
                "Replay+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.BehaviorProcessor.create().subscribeWith(new MyRx2Subscriber()),
                "Behavior+consumer", "Rx2Flowable");

        checkMemory(() -> io.reactivex.processors.UnicastProcessor.create().subscribeWith(new MyRx2Subscriber()),
                "Unicast+consumer", "Rx2Flowable");

        // ---------------------------------------------------------------------------------------------------------------------
    }
}
