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

import org.reactivestreams.*;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import rsc.subscriber.SubscriptionHelper;
import rx.exceptions.Exceptions;

final class ResourceFlowableDoOnNext<T> extends ResourceFlowable<T> {

    final ResourceFlowable<T> source;

    final Consumer<? super T> onNext;

    final Consumer<? super T> release;

    ResourceFlowableDoOnNext(ResourceFlowable<T> source, Consumer<? super T> onNext) {
        this.source = source;
        this.onNext = onNext;
        this.release = source.release();
    }

    @Override
    public Consumer<? super T> release() {
        return release;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new RFDoOnNextSubscriber<>(s, onNext, release));
    }

    static final class RFDoOnNextSubscriber<T> implements Subscriber<T>, Subscription {

        final Subscriber<? super T> actual;

        final Consumer<? super T> onNext;

        final Consumer<? super T> release;

        Subscription upstream;

        boolean done;

        RFDoOnNextSubscriber(Subscriber<? super T> actual, Consumer<? super T> onNext, Consumer<? super T> release) {
            this.actual = actual;
            this.onNext = onNext;
            this.release = release;
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                ResourceFlowable.releaseItem(t, release);
            } else {
                try {
                    onNext.accept(t);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.cancel();
                    ResourceFlowable.releaseItem(t, release);
                    done = true;
                    actual.onError(ex);
                    return;
                }

                actual.onNext(t);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
            } else {
                actual.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (!done) {
                actual.onComplete();
            }
        }
    }
}
