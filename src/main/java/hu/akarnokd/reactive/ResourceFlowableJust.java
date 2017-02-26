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

import java.util.concurrent.atomic.AtomicInteger;

import org.reactivestreams.*;

import io.reactivex.functions.Consumer;

final class ResourceFlowableJust<T> extends ResourceFlowable<T> {

    final T item;

    final Consumer<? super T> release;

    ResourceFlowableJust(T item, Consumer<? super T> release) {
        this.item = item;
        this.release = release;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new ResourceScalarSubscription<>(subscriber, item, release));
    }

    @Override
    public Consumer<? super T> release() {
        return release;
    }

    static final class ResourceScalarSubscription<T>
    extends AtomicInteger implements Subscription {
        private static final long serialVersionUID = -4021292785194851945L;

        final Subscriber<? super T> actual;

        final T item;

        final Consumer<? super T> release;

        static final int START = 0;
        static final int REQUESTED = 1;
        static final int COMPLETE = 2;
        static final int CANCELLED = 3;

        ResourceScalarSubscription(Subscriber<? super T> actual, T item, Consumer<? super T> release) {
            this.actual = actual;
            this.item = item;
            this.release = release;
        }

        @Override
        public void request(long n) {
            if (compareAndSet(START, REQUESTED)) {
                actual.onNext(item);
                if (compareAndSet(REQUESTED, COMPLETE)) {
                    actual.onComplete();
                }
            }
        }

        @Override
        public void cancel() {
            int s = getAndSet(CANCELLED);
            if (s == START) {
                releaseItem(item, release);
            }
        }
    }
}
