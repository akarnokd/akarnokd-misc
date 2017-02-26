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

import java.util.concurrent.Callable;

import org.reactivestreams.Subscriber;

import io.reactivex.functions.Consumer;
import io.reactivex.internal.subscriptions.EmptySubscription;
import rx.exceptions.Exceptions;

final class ResourceFlowableDefer<T> extends ResourceFlowable<T> {

    final Callable<? extends ResourceFlowable<T>> call;

    final Consumer<? super T> release;

    ResourceFlowableDefer(Callable<? extends ResourceFlowable<T>> call, Consumer<? super T> release) {
        this.call = call;
        this.release = release;
    }

    @Override
    public Consumer<? super T> release() {
        return release;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        ResourceFlowable<T> rf;

        try {
            rf = call.call();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptySubscription.error(ex, subscriber);
            return;
        }

        rf.subscribe(subscriber);
    }
}
