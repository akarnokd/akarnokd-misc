package hu.akarnokd.rxjava2;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.Scheduler.Worker;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.TestScheduler;

public class ThrottleSampleTest {

    @Test
    public void test() {
        TestScheduler tsch = new TestScheduler();

        Flowable.fromArray(
                100,                // should emit 100 at T=100
                110, 120, 130, 150, // should emit 150 at T=200
                250, 260,           // should emit 260 at T=300
                400                 // should emit 400 at T=400
        )
        .flatMap(v -> Flowable.timer(v, TimeUnit.MILLISECONDS, tsch).map(w -> v))
        .compose(throttleFirstSample(100, TimeUnit.MILLISECONDS, tsch))
        .subscribe(v -> System.out.println(v + " at T=" + tsch.now(TimeUnit.MILLISECONDS)));

        tsch.advanceTimeBy(1, TimeUnit.SECONDS);
    }

    static final Exception RESTART_INDICATOR = new Exception();

    static <T> FlowableTransformer<T, T> throttleFirstSample(long time, TimeUnit unit, Scheduler scheduler) {
        return f ->
            f
            .publish(g ->
                g
                .take(1)
                .concatWith(
                    g
                    .buffer(time, unit, scheduler)
                    .map(v -> {
                        if (v.isEmpty()) {
                            throw RESTART_INDICATOR;
                        }
                        return v.get(v.size() - 1);
                    })
                )
                .retry(e -> e == RESTART_INDICATOR)
            )
        ;
    }

    @Test
    public void testObservable() {
        TestScheduler tsch = new TestScheduler();

        Observable.fromArray(
                100,                // should emit 100 at T=100
                110, 120, 130, 150, // should emit 150 at T=200
                250, 260,           // should emit 260 at T=300
                400                 // should emit 400 at T=400
        )
        .flatMap(v -> Observable.timer(v, TimeUnit.MILLISECONDS, tsch).map(w -> v))
        .compose(throttleFirstSampleObservable(100, TimeUnit.MILLISECONDS, tsch))
        .subscribe(v -> System.out.println(v + " at T=" + tsch.now(TimeUnit.MILLISECONDS)));

        tsch.advanceTimeBy(1, TimeUnit.SECONDS);
    }

    static <T> ObservableTransformer<T, T> throttleFirstSampleObservable(long time, TimeUnit unit, Scheduler scheduler) {
        return f -> new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                f.subscribe(new ThrottleFirstSampleObserver<T>(observer, time, unit, scheduler.createWorker()));
            }
        };
    }

    static final class ThrottleFirstSampleObserver<T>
    extends AtomicInteger
    implements Observer<T>, Disposable, Runnable {

        private static final long serialVersionUID = 205628968660185683L;

        static final Object TIMEOUT = new Object();

        final Observer<? super T> actual;

        final Queue<Object> queue;

        final Worker worker;

        final long time;

        final TimeUnit unit;

        Disposable upstream;

        boolean latestMode;

        T latest;

        volatile boolean done;
        Throwable error;

        volatile boolean disposed;

        ThrottleFirstSampleObserver(Observer<? super T> actual, long time, TimeUnit unit, Worker worker) {
            this.actual = actual;
            this.time = time;
            this.unit = unit;
            this.worker = worker;
            this.queue = new ConcurrentLinkedQueue<>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            upstream = d;
            actual.onSubscribe(this);
        }

        @Override
        public void onNext(T t) {
            queue.offer(t);
            drain();
        }

        @Override
        public void onError(Throwable e) {
            error = e;
            done = true;
            drain();
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void dispose() {
            disposed = true;
            upstream.dispose();
            worker.dispose();
            if (getAndIncrement() == 0) {
                queue.clear();
                latest = null;
            }
        }

        @Override
        public void run() {
            queue.offer(TIMEOUT);
            drain();
        }

        @SuppressWarnings("unchecked")
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Observer<? super T> a = actual;
            Queue<Object> q = queue;

            for (;;) {

                for (;;) {
                    if (disposed) {
                        q.clear();
                        latest = null;
                        return;
                    }


                    boolean d = done;
                    Object v = q.poll();
                    boolean empty = v == null;

                    if (d && empty) {
                        if (latestMode) {
                            T u = latest;
                            latest = null;
                            if (u != null) {
                                a.onNext(u);
                            }
                        }
                        Throwable ex = error;
                        if (ex != null) {
                            a.onError(ex);
                        } else {
                            a.onComplete();
                        }
                        worker.dispose();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    if (latestMode) {
                        if (v == TIMEOUT) {
                            T u = latest;
                            latest = null;
                            if (u != null) {
                                a.onNext(u);
                                worker.schedule(this, time, unit);
                            } else {
                                latestMode = false;
                            }
                        } else {
                            latest = (T)v;
                        }
                    } else {
                        latestMode = true;
                        a.onNext((T)v);
                        worker.schedule(this, time, unit);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }
}
