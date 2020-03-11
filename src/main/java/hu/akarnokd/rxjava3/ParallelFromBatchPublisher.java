package hu.akarnokd.rxjava3;

import java.util.concurrent.atomic.*;

import org.reactivestreams.*;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.internal.fuseable.*;
import io.reactivex.rxjava3.internal.queue.SpscArrayQueue;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.parallel.ParallelFlowable;

public final class ParallelFromBatchPublisher<T> extends ParallelFlowable<T> {

    final Publisher<? extends T> source;

    final int parallelism;

    final int prefetch;

    public ParallelFromBatchPublisher(Publisher<? extends T> source, int parallelism, int prefetch) {
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

            int missed = 1;
            outer:
            for (;;) {

                if (cancelled) {
                    queue.clear();
                } else {
                    for (RailSubscription<T> rs : rails) {

                        long r = rs.get();

                        if (r != Long.MIN_VALUE) {
                            long e = rs.emitted;

                            while (r != e) {

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
                                    continue outer;
                                }

                                boolean empty = item == null;

                                if (empty) {
                                    for (RailSubscription<T> rs0 : rails) {
                                        rs0.downstream.onComplete();
                                    }
                                    cancelled = true;
                                    break outer;
                                }

                                rs.downstream.onNext(item);

                                e++;
                            }

                            rs.emitted = e;

                            if (e == r) {
                                if (queue.isEmpty()) {
                                    for (RailSubscription<T> rs0 : rails) {
                                        rs0.downstream.onComplete();
                                    }
                                    cancelled = true;
                                    break outer;
                                }
                            }
                        }
                    }
                }

                int w = get();
                if (w == missed) {
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }

        void drainAsync() {
            SimpleQueue<T> queue = this.queue;
            RailSubscription<T>[] rails = this.rails;

            int c = this.consumed;
            int limit = this.limit;

            int missed = 1;
            outer:
            for (;;) {

                if (cancelled) {
                    queue.clear();
                } else {
                    rails:
                    for (RailSubscription<T> rs : rails) {

                        long r = rs.get();

                        if (r != Long.MIN_VALUE) {
                            long e = rs.emitted;

                            while (r != e) {

                                boolean d = done;

                                if (d) {
                                    Throwable ex = error;
                                    if (ex != null) {
                                        for (RailSubscription<T> rs0 : rails) {
                                            rs0.downstream.onError(ex);
                                        }
                                        cancelled = true;
                                        break outer;
                                    }
                                }

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
                                    continue outer;
                                }

                                boolean empty = item == null;

                                if (d && empty) {
                                    for (RailSubscription<T> rs0 : rails) {
                                        rs0.downstream.onComplete();
                                    }
                                    cancelled = true;
                                    break outer;
                                }

                                if (empty) {
                                    rs.emitted = e;
                                    break rails;
                                }

                                rs.downstream.onNext(item);

                                e++;

                                if (++c == limit) {
                                    c = 0;
                                    upstream.request(limit);
                                }
                            }

                            rs.emitted = e;

                            if (e == r) {
                                if (done && queue.isEmpty()) {
                                    for (RailSubscription<T> rs0 : rails) {
                                        rs0.downstream.onComplete();
                                    }
                                    cancelled = true;
                                    break outer;
                                }
                            }
                        }
                    }
                }

                int w = get();
                if (w == missed) {
                    this.consumed = c;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
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