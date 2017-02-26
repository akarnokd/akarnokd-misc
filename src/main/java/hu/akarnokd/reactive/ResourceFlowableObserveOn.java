/*
 * Copyright 2015-2017 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package hu.akarnokd.reactive;

import java.util.concurrent.atomic.*;

import org.reactivestreams.*;

import hu.akarnokd.reactive.ResourceScheduler.ResourceTask;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.queue.SpscArrayQueue;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.BackpressureHelper;

final class ResourceFlowableObserveOn<T> extends ResourceFlowable<T> {

    final ResourceFlowable<T> source;

    final ResourceScheduler scheduler;

    final int bufferSize;

    final Consumer<? super T> release;

    ResourceFlowableObserveOn(ResourceFlowable<T> source, ResourceScheduler scheduler, int bufferSize) {
        this.source = source;
        this.scheduler = scheduler;
        this.bufferSize = bufferSize;
        this.release = source.release();
    }

    @Override
    public Consumer<? super T> release() {
        return release;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        source.subscribe(new RFObserveOnSubscriber<>(subscriber, scheduler.createWorker(), release, bufferSize));
    }

    static final class RFObserveOnSubscriber<T> extends AtomicInteger
    implements Subscriber<T>, Subscription, ResourceTask {

        private static final long serialVersionUID = -436873336763533104L;

        final Subscriber<? super T> actual;

        final ResourceScheduler.ResourceWorker worker;

        final Consumer<? super T> release;

        final SpscArrayQueue<T> queue;

        final AtomicLong requested;

        final int bufferSize;

        final int limit;

        Subscription upstream;

        volatile boolean done;
        Throwable error;

        volatile boolean cancelled;

        long emitted;

        long consumed;

        RFObserveOnSubscriber(Subscriber<? super T> actual, ResourceScheduler.ResourceWorker worker, Consumer<? super T> release, int bufferSize) {
            this.actual = actual;
            this.worker = worker;
            this.release = release;
            this.bufferSize = bufferSize;
            this.limit = bufferSize - (bufferSize >> 2);
            this.queue = new SpscArrayQueue<>(bufferSize);
            this.requested = new AtomicLong();
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
                schedule();
            }
        }

        @Override
        public void cancel() {
            if (!cancelled) {
                cancelled = true;
                upstream.cancel();
                worker.dispose();
                cleanup();
            }
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                actual.onSubscribe(this);

                s.request(bufferSize);
            }
        }

        @Override
        public void onNext(T t) {
            queue.offer(t);
            schedule();
        }

        @Override
        public void onError(Throwable t) {
            error = t;
            done = true;
            schedule();
        }

        @Override
        public void onComplete() {
            done = true;
            schedule();
        }

        void schedule() {
            if (getAndIncrement() == 0) {
                worker.schedule(this);
            }
        }

        void cleanup() {
            if (getAndIncrement() == 0) {
                clearQueueWip();
            }
        }

        void clearQueueWip() {
            do {
                clearQueue(this.queue, this.release);
            } while (decrementAndGet() != 0);
        }

        void clearQueue(SpscArrayQueue<T> queue, Consumer<? super T> release) {
            for (;;) {
                T v = queue.poll();
                if (v == null) {
                    break;
                }
                releaseItem(v, release);
            }
        }

        @Override
        public void onCancel() {
            clearQueueWip();
        }

        @Override
        public void run() {
            int missed = 1;

            Subscriber<? super T> a = actual;
            long e = emitted;
            long f = consumed;
            long lim = limit;
            SpscArrayQueue<T> q = queue;

            for (;;) {

                long r = requested.get();

                while (e != r) {
                    if (cancelled) {
                        clearQueue(q, release);
                        break;
                    } else {
                        boolean d = done;
                        T v = q.poll();
                        boolean empty = v == null;

                        if (d && empty) {
                            Throwable ex = error;
                            if (ex != null) {
                                a.onError(ex);
                            } else {
                                a.onComplete();
                            }
                            worker.dispose();
                            break;
                        }

                        if (empty) {
                            break;
                        }

                        a.onNext(v);

                        e++;
                        if (++f == lim) {
                            f = 0L;
                            upstream.request(lim);
                        }
                    }
                }

                if (e == r) {
                    if (cancelled) {
                        clearQueue(q, release);
                        break;
                    } else {
                        if (done && q.isEmpty()) {
                            Throwable ex = error;
                            if (ex != null) {
                                a.onError(ex);
                            } else {
                                a.onComplete();
                            }
                            worker.dispose();
                        }
                    }
                }

                int w = get();
                if (w == missed) {
                    emitted = e;
                    consumed = f;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }
    }
}
