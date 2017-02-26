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

final class ResourceFlowablePublisher<T> extends ResourceFlowable<T> {

    final Publisher<? extends T> source;

    final Consumer<? super T> release;

    ResourceFlowablePublisher(Publisher<? extends T> source, Consumer<? super T> release) {
        this.source = source;
        this.release = release;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        source.subscribe(subscriber);
    }

    @Override
    public Consumer<? super T> release() {
        return release;
    }
}
