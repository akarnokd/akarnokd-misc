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

import io.reactivex.functions.*;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.plugins.RxJavaPlugins;
import rsc.subscriber.SubscriptionHelper;
import rx.exceptions.Exceptions;

final class ResourceFlowableMap<T, R> extends ResourceFlowable<R> {

    final ResourceFlowable<T> source;

    final Function<? super T, ? extends R> mapper;

    final Consumer<? super R> release;

    ResourceFlowableMap(ResourceFlowable<T> source, Function<? super T, ? extends R> mapper, Consumer<? super R> release) {
        this.source = source;
        this.mapper = mapper;
        this.release = release;
    }

    @Override
    public Consumer<? super R> release() {
        return release;
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new RFMapSubscriber<>(s, mapper, source.release()));
    }

    static final class RFMapSubscriber<T, R> implements Subscriber<T>, Subscription {

        final Subscriber<? super R> actual;

        final Function<? super T, ? extends R> mapper;

        final Consumer<? super T> release;

        Subscription upstream;

        boolean done;

        RFMapSubscriber(Subscriber<? super R> actual, Function<? super T, ? extends R> mapper, Consumer<? super T> release) {
            this.actual = actual;
            this.mapper = mapper;
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
                R v;

                try {
                    v = ObjectHelper.requireNonNull(mapper.apply(t), "The mapper returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.cancel();
                    ResourceFlowable.releaseItem(t, release);
                    done = true;
                    actual.onError(ex);
                    return;
                }

                ResourceFlowable.releaseItem(t, release);

                actual.onNext(v);
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
