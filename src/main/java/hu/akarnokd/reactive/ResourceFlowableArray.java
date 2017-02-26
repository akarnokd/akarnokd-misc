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

import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.*;

import io.reactivex.functions.Consumer;
import io.reactivex.internal.util.BackpressureHelper;
import rsc.subscriber.SubscriptionHelper;

final class ResourceFlowableArray<T> extends ResourceFlowable<T> {

    final T[] items;

    final Consumer<? super T> release;

    ResourceFlowableArray(T[] items, Consumer<? super T> release) {
        this.items = items;
        this.release = release;
    }

    @Override
    public Consumer<? super T> release() {
        return release;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new RFArraySubscription<>(subscriber, release, items));
    }

    static final class RFArraySubscription<T>
    extends AtomicLong
    implements Subscription {

        private static final long serialVersionUID = -5292995154043177106L;

        final Subscriber<? super T> actual;

        final Consumer<? super T> release;

        final T[] items;

        int index;

        volatile boolean cancelled;

        RFArraySubscription(Subscriber<? super T> actual, Consumer<? super T> release, T[] items) {
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
                    int idx = index;
                    T[] array = items;
                    int len = array.length;
                    long f = 0;
                    for (;;) {

                        for (; idx != len && f != n; idx++, f++) {
                            if (cancelled) {
                                Consumer<? super T> rel = release;
                                while (idx != len) {
                                    releaseItem(array[idx++], rel);
                                }
                                return;
                            }
                            T t = array[idx];
                            if (t == null) {
                                Consumer<? super T> rel = release;
                                while (idx != len) {
                                    releaseItem(array[idx++], rel);
                                }
                                a.onError(new NullPointerException());
                                return;
                            } else {
                                a.onNext(t);
                            }
                        }

                        if (idx == len) {
                            if (!cancelled) {
                                a.onComplete();
                            }
                            return;
                        }

                        long m = get();
                        if (n == m) {
                            index = idx;
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
                    T[] a = items;
                    int n = a.length;
                    Consumer<? super T> r = release;
                    for (int i = index; i < n; i++) {
                        releaseItem(a[i], r);
                    }
                }
            }
        }
    }
}
