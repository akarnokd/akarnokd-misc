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

import com.annimon.stream.Objects;

import io.reactivex.*;
import io.reactivex.functions.*;

/**
 * A special Flowable that makes sure unconsumed items
 * are released.
 * @param <T> the value type
 */
public abstract class ResourceFlowable<T> {

    public final void subscribe(Subscriber<? super T> subscriber) {
        Objects.requireNonNull(subscriber, "subscriber is null");
        try {
            subscribeActual(subscriber);
        } catch (NullPointerException ex) {
            throw ex;
        } catch (Throwable ex) {
            NullPointerException npe = new NullPointerException();
            npe.initCause(ex);
            throw npe;
        }
    }

    protected abstract void subscribeActual(Subscriber<? super T> subscriber);

    public abstract Consumer<? super T> release();

    // --------------------------------------------------------------------------------

    public static <T> ResourceFlowable<T> just(T item, Consumer<? super T> release) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    public static <T> ResourceFlowable<T> fromArray(Consumer<? super T> release, T... items) {
        int n = items.length;
        if (n == 1) {
            return just(items[0], release);
        }
        // TODO
        throw new UnsupportedOperationException();
    }

    public static <T> ResourceFlowable<T> fromIterable(Iterable<? extends T> items, Consumer<? super T> release) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public static <T> ResourceFlowable<T> fromPublisher(Publisher<? extends T> source, Consumer<? super T> release) {
        // TODO
        throw new UnsupportedOperationException();
    }

    // --------------------------------------------------------------------------------

    public final ResourceFlowable<T> subscribeOn(Scheduler scheduler) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public final ResourceFlowable<T> observeOn(Scheduler scheduler) {
        return new ResourceFlowableObserveOn<>(this, scheduler, Flowable.bufferSize());
    }

    public final ResourceFlowable<T> doOnNext(Consumer<? super T> onNext) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public final <R> ResourceFlowable<R> map(Function<? super T, ? extends R> mapper, Consumer<? super R> release) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public final <R> ResourceFlowable<R> flatMap(Function<? super T, ? extends ResourceFlowable<? extends R>> mapper) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public final <R> ResourceFlowable<R> flatMap(Function<? super T, ? extends ResourceFlowable<? extends R>> mapper, int maxConcurrency) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public final <R> ResourceFlowable<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper, Consumer<? super R> release) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public final <R> ResourceFlowable<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper, int maxConcurrency, Consumer<? super R> release) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public final <R> ResourceFlowable<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper, Consumer<? super R> release) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public final Flowable<T> toFlowable() {
        // TODO
        throw new UnsupportedOperationException();
    }
}
