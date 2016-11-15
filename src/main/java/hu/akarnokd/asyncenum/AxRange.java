/*
 * Copyright 2016 David Karnok
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

package hu.akarnokd.asyncenum;

import java.util.concurrent.*;

import rx.subscriptions.CompositeSubscription;

public final class AxRange extends Ax<Integer> {

    final int start;
    final int count;

    public AxRange(int start, int count) {
        this.start = start;
        this.count = count;
    }

    @Override
    public IAsyncEnumerator<Integer> enumerator() {
        return new AxRangeEnumerator(start, count);
    }

    static final class AxRangeEnumerator implements IAsyncEnumerator<Integer> {
        final long end;
        long index;

        static final CompletionStage<Boolean> TRUE = CompletableFuture.completedFuture(true);

        static final CompletionStage<Boolean> FALSE = CompletableFuture.completedFuture(false);

        AxRangeEnumerator(int start, int count) {
            this.index = start - 1;
            this.end = (long)start + count;
        }

        @Override
        public CompletionStage<Boolean> moveNext(CompositeSubscription token) {
            long i = index + 1;
            if (i == end) {
                return FALSE;
            }
            index = i;
            return TRUE;
        }

        @Override
        public Integer current() {
            return (int)index;
        }
    }
}
