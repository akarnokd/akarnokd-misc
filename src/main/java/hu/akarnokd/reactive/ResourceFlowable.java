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

import io.reactivex.Flowable;
import io.reactivex.functions.*;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.plugins.RxJavaPlugins;

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

    /**
     * Releases an item by calling the Consumer and sending all Exceptions
     * to the RxJavaPlugins.onError.
     * @param <T> the value type
     * @param item the item to release
     * @param release the callback to release the item
     */
    protected static <T> void releaseItem(T item, Consumer<? super T> release) {
        try {
            release.accept(item);
        } catch (Throwable ex) {
            RxJavaPlugins.onError(ex);
        }
    }

    // --------------------------------------------------------------------------------

    public static <T> ResourceFlowable<T> just(T item, Consumer<? super T> release) {
        ObjectHelper.requireNonNull(item, "item is null");
        ObjectHelper.requireNonNull(release, "release is null");
        return new ResourceFlowableJust<>(item, release);
    }

    @SafeVarargs
    public static <T> ResourceFlowable<T> fromArray(Consumer<? super T> release, T... items) {
        ObjectHelper.requireNonNull(release, "release is null");
        int n = items.length;
        if (n == 1) {
            return just(items[0], release);
        }
        return new ResourceFlowableArray<>(items, release);
    }

    public static <T> ResourceFlowable<T> fromIterable(Iterable<? extends T> items, Consumer<? super T> release) {
        ObjectHelper.requireNonNull(items, "items is null");
        ObjectHelper.requireNonNull(release, "release is null");
        return new ResourceFlowableIterable<>(items, release);
    }

    public static <T> ResourceFlowable<T> fromPublisher(Publisher<? extends T> source, Consumer<? super T> release) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(release, "release is null");
        return new ResourceFlowablePublisher<>(source, release);
    }

    // --------------------------------------------------------------------------------

    public final ResourceFlowable<T> observeOn(ResourceScheduler scheduler) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return new ResourceFlowableObserveOn<>(this, scheduler, Flowable.bufferSize());
    }

    public final ResourceFlowable<T> withRelease(Consumer<? super T> release) {
        ObjectHelper.requireNonNull(release, "release is null");
        return new ResourceFlowableWithRelease<>(this, release);
    }

    public final ResourceFlowable<T> subscribeOn(ResourceScheduler scheduler) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        // TODO
        throw new UnsupportedOperationException();
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

    public final <R> Flowable<R> toFlowable(Function<? super T, ? extends R> extract) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
