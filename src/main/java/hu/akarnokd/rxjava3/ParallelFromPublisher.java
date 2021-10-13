package hu.akarnokd.rxjava3;

import java.util.concurrent.atomic.*;

import org.reactivestreams.*;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.operators.*;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.parallel.ParallelFlowable;

public final class ParallelFromPublisher<T> extends ParallelFlowable<T> {

    final Publisher<? extends T> source;

    final int parallelism;

    final int prefetch;

    public ParallelFromPublisher(Publisher<? extends T> source, int parallelism, int prefetch) {
        this.source = source;
        this.parallelism = parallelism;
        this.prefetch = prefetch;
    }

    @Override
    public int parallelism() {
        return parallelism;
    }

    @Override
    public void subscribe(Subscriber<? super T>[] subscribers) {
        if (!validate(subscribers)) {
            return;
        }

        source.subscribe(new ParallelDispatcher<>(subscribers, prefetch));
    }

    static final class ParallelDispatcher<T>
    extends AtomicInteger
    implements FlowableSubscriber<T> {

        private static final long serialVersionUID = -4470634016609963609L;

        final RailSubscription<T>[] rails;

        final int prefetch;

        final int limit;

        Subscription upstream;

        SimpleQueue<T> queue;

        volatile boolean done;
        Throwable error;

        final AtomicInteger cancel;

        volatile boolean cancelled;

        final AtomicLong ready;

        int index;

        int consumed;

        int sourceMode;

        @SuppressWarnings("unchecked")
        ParallelDispatcher(Subscriber<? super T>[] subscribers, int prefetch) {
            int n = subscribers.length;
            this.rails = new RailSubscription[n];
            for (int i = 0; i < subscribers.length; i++) {
                this.rails[i] = new RailSubscription<>(this, subscribers[i]);
            }
            this.prefetch = prefetch;
            this.limit = prefetch - (prefetch >> 2);
            this.cancel = new AtomicInteger(n);
            this.ready = new AtomicLong(n);
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                if (s instanceof QueueSubscription) {
                    @SuppressWarnings("unchecked")
                    QueueSubscription<T> qs = (QueueSubscription<T>) s;

                    int m = qs.requestFusion(QueueFuseable.ANY | QueueFuseable.BOUNDARY);
                    if (m == QueueFuseable.SYNC) {
                        sourceMode = m;
                        queue = qs;
                        done = true;
                        subscribeRails();
                        return;
                    }
                    if (m == QueueFuseable.ASYNC) {
                        sourceMode = m;
                        queue = qs;
                        subscribeRails();
                        s.request(prefetch);
                        return;
                    }
                }

                queue = new SpscArrayQueue<>(prefetch);

                subscribeRails();

                s.request(prefetch);
            }
        }

        void subscribeRails() {
            for (RailSubscription<T> rs : rails) {
                rs.subscribe();
            }
        }

        @Override
        public void onNext(@NonNull T t) {
            if (sourceMode == QueueFuseable.NONE) {
                if (!queue.offer(t)) {
                    onError(new MissingBackpressureException("Queue full?!"));
                    return;
                }
            }
            drain();
        }

        @Override
        public void onError(Throwable t) {
            error = t;
            done = true;
            drain();
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            if (sourceMode == QueueFuseable.SYNC) {
                drainSync();
            } else {
                drainAsync();
            }
        }

        void drainSync() {
            SimpleQueue<T> queue = this.queue;
            RailSubscription<T>[] rails = this.rails;
            int n = rails.length;

            int index = this.index;
            int notReady = 0;

            int missed = 1;
            for (;;) {

                if (cancelled) {
                    queue.clear();
                } else {

                    RailSubscription<T> rs = rails[index];

                    long r = rs.get();
                    long e = rs.emitted;

                    if (r != Long.MIN_VALUE && r != e) {
                        T item;
                        try {
                            item = queue.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            upstream.cancel();
                            for (RailSubscription<T> rs0 : rails) {
                                rs0.downstream.onError(ex);
                            }
                            cancelled = true;
                            continue;
                        }

                        if (item != null) {

                            rs.downstream.onNext(item);

                            rs.emitted = e + 1;

                            if (++index == n) {
                                index = 0;
                            }
                            notReady = 0;
                            continue;
                        }
                    } else {
                        if (++index == n) {
                            index = 0;
                        }
                        if (++notReady != n) {
                            continue;
                        }
                    }
                    if (queue.isEmpty()) {
                        for (RailSubscription<T> rs0 : rails) {
                            rs0.downstream.onComplete();
                        }
                        cancelled = true;
                    }
                }

                int w = get();
                if (w == missed) {
                    this.index = index;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
                notReady = 0;
            }
        }

        void drainAsync() {
            SimpleQueue<T> queue = this.queue;
            RailSubscription<T>[] rails = this.rails;
            int n = rails.length;

            int index = this.index;
            int notReady = 0;
            int c = this.consumed;
            int limit = this.limit;

            int missed = 1;
            for (;;) {

                if (cancelled) {
                    queue.clear();
                } else {

                    boolean d = done;
                    if (d) {
                        Throwable ex = error;
                        if (ex != null) {
                            for (RailSubscription<T> rs : rails) {
                                rs.downstream.onError(ex);
                            }
                            cancelled = true;
                            continue;
                        }
                    }

                    RailSubscription<T> rs = rails[index];

                    long r = rs.get();
                    long e = rs.emitted;

                    if (r != Long.MIN_VALUE && r != e) {
                        T item;
                        try {
                            item = queue.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            upstream.cancel();
                            for (RailSubscription<T> rs0 : rails) {
                                rs0.downstream.onError(ex);
                            }
                            cancelled = true;
                            continue;
                        }

                        if (item != null) {

                            rs.downstream.onNext(item);

                            rs.emitted = e + 1;

                            if (++c == limit) {
                                c = 0;
                                upstream.request(limit);
                            }

                            if (++index == n) {
                                index = 0;
                            }
                            notReady = 0;
                            continue;
                        }
                    } else {
                        if (++index == n) {
                            index = 0;
                        }
                        if (++notReady != n) {
                            continue;
                        }
                    }

                    if (done && queue.isEmpty()) {
                        for (RailSubscription<T> rs0 : rails) {
                            rs0.downstream.onComplete();
                        }
                        cancelled = true;
                        continue;
                    }
                }

                int w = get();
                if (w == missed) {
                    this.index = index;
                    this.consumed = c;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
                notReady = 0;
            }
        }

        void cancelRail() {
            if (cancel.decrementAndGet() == 0) {
                cancelled = true;
                upstream.cancel();
                drain();
            }
        }

        void requested(long m) {
            if (m == 0) {
                if (ready.decrementAndGet() == 0) {
                    drain();
                }
            } else {
                drain();
            }
        }

        static final class RailSubscription<T>
        extends AtomicLong
        implements Subscription {

            private static final long serialVersionUID = -8895167190215878854L;

            final ParallelDispatcher<T> parent;

            final Subscriber<? super T> downstream;

            long emitted;

            RailSubscription(ParallelDispatcher<T> parent, Subscriber<? super T> downstream) {
                this.parent = parent;
                this.downstream = downstream;
            }

            @Override
            public void request(long n) {
                if (SubscriptionHelper.validate(n)) {
                    parent.requested(BackpressureHelper.addCancel(this, n));
                }
            }

            @Override
            public void cancel() {
                if (getAndSet(Long.MIN_VALUE) != Long.MIN_VALUE) {
                    parent.cancelRail();
                }
            }

            void subscribe() {
                downstream.onSubscribe(this);
            }
        }
    }
}