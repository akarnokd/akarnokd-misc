package hu.akarnokd.rxjava2;

import java.util.*;

import org.reactivestreams.*;

import hu.akarnokd.rxjava2.RxSynchronousProfiler.CallStatistics;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.fuseable.*;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Hooks the onAssembly calls, times and counts the various method calls passing through it.
 * ONLY FOR SYNCHRONOUS STREAMS!
 */
@SuppressWarnings("rawtypes")
public class RxSynchronousCoarseProfiler {

    public final Map<String, CallStatistics> entries;

    public RxSynchronousCoarseProfiler() {
        entries = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public void start() {
        RxJavaPlugins.setOnFlowableAssembly(t -> {
            FlowableProfiler p = new FlowableProfiler(t);
            CallStatistics cs = new CallStatistics();
            cs.key = t.getClass().getSimpleName();
            CallStatistics cs2 = entries.putIfAbsent(cs.key, cs);

            if (cs2 == null) {
                p.stats = cs;
            } else {
                p.stats = cs2;
            }

            return p;
        });
        RxJavaPlugins.setOnSingleAssembly(t -> {
            SingleProfiler p = new SingleProfiler(t);
            CallStatistics cs = new CallStatistics();
            cs.key = t.getClass().getSimpleName();
            CallStatistics cs2 = entries.putIfAbsent(cs.key, cs);

            if (cs2 == null) {
                p.stats = cs;
            } else {
                p.stats = cs2;
            }

            return p;
        });
    }

    public void stop() {
        RxJavaPlugins.setOnFlowableAssembly(null);
        RxJavaPlugins.setOnSingleAssembly(null);
    }

    public void clear() {
        entries.clear();
    }

    public void print() {
        List<CallStatistics> list = new ArrayList<>(entries.values());

        list.sort(Comparator.comparing(CallStatistics::sumTime).reversed());

        list.forEach(v -> System.out.println(v.print()));
    }

    static final class FlowableProfiler<T> extends Flowable<T> {

        final Publisher<T> source;

        CallStatistics stats;

        FlowableProfiler(Publisher<T> source) {
            this.source = source;
        }

        @Override
        protected void subscribeActual(Subscriber<? super T> s) {
            long now = System.nanoTime();

            if (s instanceof ConditionalSubscriber) {
                source.subscribe(new ProfilerConditionalSubscriber<T>((ConditionalSubscriber<? super T>)s, stats));
            } else {
                source.subscribe(new ProfilerSubscriber<T>(s, stats));
            }

            long after = System.nanoTime();
            stats.subscribeCount++;
            stats.subscribeTime += Math.max(0, after - now);
        }

        static final class ProfilerSubscriber<T> implements FlowableSubscriber<T>, QueueSubscription<T> {

            final Subscriber<? super T> actual;

            final CallStatistics calls;

            Subscription s;

            QueueSubscription<T> qs;

            long startTime;

            ProfilerSubscriber(Subscriber<? super T> actual, CallStatistics calls) {
                this.actual = actual;
                this.calls = calls;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                if (s instanceof QueueSubscription) {
                    qs = (QueueSubscription<T>)s;
                }

                calls.onSubscribeCount++;
                startTime = System.nanoTime();

                actual.onSubscribe(this);
            }

            @Override
            public void onNext(T t) {
                actual.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                actual.onError(t);

                long after = System.nanoTime();
                calls.onErrorCount++;
                calls.onErrorTime += Math.max(0, after - startTime);
            }

            @Override
            public void onComplete() {
                actual.onComplete();

                long after = System.nanoTime();
                calls.onCompleteCount++;
                calls.onCompleteTime += Math.max(0, after - startTime);
            }

            @Override
            public boolean offer(T value) {
                throw new UnsupportedOperationException("Should not be called");
            }

            @Override
            public boolean offer(T v1, T v2) {
                throw new UnsupportedOperationException("Should not be called");
            }

            @Override
            public T poll() throws Exception {
                return qs.poll();
            }

            @Override
            public void clear() {
                qs.clear();
            }

            @Override
            public boolean isEmpty() {
                return qs.isEmpty();
            }

            @Override
            public void request(long n) {
                s.request(n);
            }

            @Override
            public int requestFusion(int mode) {
                QueueSubscription<T> qs = this.qs;
                return qs != null ? qs.requestFusion(mode) : NONE;
            }

            @Override
            public void cancel() {
                s.cancel();
            }
        }

        static final class ProfilerConditionalSubscriber<T> implements ConditionalSubscriber<T>, QueueSubscription<T> {

            final ConditionalSubscriber<? super T> actual;

            final CallStatistics calls;

            Subscription s;

            QueueSubscription<T> qs;

            long startTime;

            ProfilerConditionalSubscriber(ConditionalSubscriber<? super T> actual, CallStatistics calls) {
                this.actual = actual;
                this.calls = calls;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                if (s instanceof QueueSubscription) {
                    qs = (QueueSubscription<T>)s;
                }

                calls.onSubscribeCount++;
                startTime = System.nanoTime();

                actual.onSubscribe(this);
            }

            @Override
            public void onNext(T t) {
                actual.onNext(t);
            }

            @Override
            public boolean tryOnNext(T t) {
                return actual.tryOnNext(t);
            }

            @Override
            public void onError(Throwable t) {
                actual.onError(t);

                long after = System.nanoTime();
                calls.onErrorCount++;
                calls.onErrorTime += Math.max(0, after - startTime);
            }

            @Override
            public void onComplete() {
                actual.onComplete();

                long after = System.nanoTime();
                calls.onCompleteCount++;
                calls.onCompleteTime += Math.max(0, after - startTime);
            }

            @Override
            public boolean offer(T value) {
                throw new UnsupportedOperationException("Should not be called");
            }

            @Override
            public boolean offer(T v1, T v2) {
                throw new UnsupportedOperationException("Should not be called");
            }

            @Override
            public T poll() throws Exception {
                return qs.poll();
            }

            @Override
            public void clear() {
                qs.clear();
            }

            @Override
            public boolean isEmpty() {
                return qs.isEmpty();
            }

            @Override
            public void request(long n) {
                s.request(n);
            }

            @Override
            public int requestFusion(int mode) {
                QueueSubscription<T> qs = this.qs;
                return qs != null ? qs.requestFusion(mode) : NONE;
            }

            @Override
            public void cancel() {
                s.cancel();
            }
        }
    }


    static final class SingleProfiler<T> extends Single<T> {

        final Single<T> source;

        CallStatistics stats;

        SingleProfiler(Single<T> source) {
            this.source = source;
        }

        @Override
        protected void subscribeActual(SingleObserver<? super T> s) {
            long now = System.nanoTime();

            source.subscribe(new ProfilerSingleObserver<T>(s, stats));

            long after = System.nanoTime();
            stats.subscribeCount++;
            stats.subscribeTime += Math.max(0, after - now);
        }

        static final class ProfilerSingleObserver<T> implements SingleObserver<T>, Disposable {

            final SingleObserver<? super T> actual;

            final CallStatistics calls;

            Disposable s;

            QueueSubscription<T> qs;

            long startTime;

            ProfilerSingleObserver(SingleObserver<? super T> actual, CallStatistics calls) {
                this.actual = actual;
                this.calls = calls;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable s) {
                this.s = s;
                if (s instanceof QueueSubscription) {
                    qs = (QueueSubscription<T>)s;
                }

                calls.onSubscribeCount++;
                startTime = System.nanoTime();

                actual.onSubscribe(this);
            }

            @Override
            public void onSuccess(T t) {
                actual.onSuccess(t);

                long after = System.nanoTime();
                calls.onCompleteCount++;
                calls.onCompleteTime += Math.max(0, after - startTime);
            }

            @Override
            public void onError(Throwable t) {
                actual.onError(t);

                long after = System.nanoTime();
                calls.onErrorCount++;
                calls.onErrorTime += Math.max(0, after - startTime);
            }

            @Override
            public void dispose() {
                s.dispose();
            }

            @Override
            public boolean isDisposed() {
                return s.isDisposed();
            }
        }
    }
}
