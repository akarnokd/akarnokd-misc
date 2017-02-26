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

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.*;

import io.reactivex.functions.Consumer;
import io.reactivex.internal.subscriptions.EmptySubscription;
import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.plugins.RxJavaPlugins;
import rsc.subscriber.SubscriptionHelper;
import rx.exceptions.Exceptions;

final class ResourceFlowableIterable<T> extends ResourceFlowable<T> {

    final Iterable<? extends T> items;

    final Consumer<? super T> release;

    ResourceFlowableIterable(Iterable<? extends T> items, Consumer<? super T> release) {
        this.items = items;
        this.release = release;
    }

    @Override
    public Consumer<? super T> release() {
        return release;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        Iterator<? extends T> it;
        boolean b;
        try {
            it = items.iterator();

            b = it.hasNext();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptySubscription.error(ex, subscriber);
            return;
        }

        if (!b) {
            EmptySubscription.complete(subscriber);
            return;
        }

        subscriber.onSubscribe(new RFIteratorSubscription<>(subscriber, release, it));
    }

    static final class RFIteratorSubscription<T>
    extends AtomicLong
    implements Subscription {

        private static final long serialVersionUID = -5292995154043177106L;

        final Subscriber<? super T> actual;

        final Consumer<? super T> release;

        final Iterator<? extends T> items;

        volatile boolean cancelled;

        RFIteratorSubscription(Subscriber<? super T> actual, Consumer<? super T> release, Iterator<? extends T> items) {
            super();
            this.actual = actual;
            this.release = release;
            this.items = items;
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                if (BackpressureHelper.add(this, n) == 0) {
                    Subscriber<? super T> a = actual;
                    Iterator<? extends T> it = items;
                    long f = 0;

                    for (;;) {

                        while (f != n) {
                            if (cancelled) {
                                releaseRest(it, release);
                                return;
                            }

                            T v;

                            try {
                                v = it.next();
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                if (!cancelled) {
                                    a.onError(ex);
                                }
                                return;
                            }

                            a.onNext(v);

                            boolean b;

                            try {
                                b = it.hasNext();
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                if (!cancelled) {
                                    a.onError(ex);
                                }
                                return;
                            }

                            if (!b) {
                                if (!cancelled) {
                                    a.onComplete();
                                }
                                return;
                            }

                            f++;
                        }

                        long m = get();
                        if (n == m) {
                            n = addAndGet(-n);
                            if (n == 0) {
                                break;
                            }
                            f = 0;
                        } else {
                            n = m;
                        }
                    }
                }
            }
        }

        @Override
        public void cancel() {
            if (!cancelled) {
                cancelled = true;

                if (BackpressureHelper.add(this, 1) == 0) {
                    releaseRest(items, release);
                }
            }
        }

        void releaseRest(Iterator<? extends T> it, Consumer<? super T> rel) {
            try {
                while (it.hasNext()) {
                    releaseItem(it.next(), rel);
                }
            } catch (Throwable ex) {
                RxJavaPlugins.onError(ex);
            }
        }
    }
}
