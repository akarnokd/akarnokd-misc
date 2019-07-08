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

package hu.akarnokd.queue;

import java.util.concurrent.atomic.AtomicLongArray;

import rx.internal.util.unsafe.Pow2;

public final class SpscLongArrayQueue {
    final AtomicLongArray buffer;
    final int mask;

    public SpscLongArrayQueue(int capacity) {
        int c = Pow2.roundToPowerOfTwo(capacity) << 1;
        buffer = new AtomicLongArray(c + 2);
        mask = c - 1;
    }

    int calcOffset(long index, int mask) {
        return 2 + ((int)index & mask);
    }

    public boolean offer(long value) {
        final AtomicLongArray a = buffer;
        final int m = mask;
        final long pi = a.get(0);

        int offset = calcOffset(pi, m);
        int offset1 = offset + 1;

        if (a.get(offset1) != 0L) {
            return false;
        }

        a.lazySet(offset, value);
        a.lazySet(offset1, 1L);
        a.lazySet(0, pi + 2);
        return true;
    }

    public long peek(boolean[] hasValue) {
        final AtomicLongArray a = buffer;
        final int m = mask;
        final long ci = a.get(1);

        int offset = calcOffset(ci, m);

        long v = a.get(offset + 1);
        if (v == 0L) {
            hasValue[0] = false;
            return 0L;
        }
        hasValue[0] = true;
        return a.get(offset);
    }

    public boolean hasValue() {
        final AtomicLongArray a = buffer;
        final int m = mask;
        final long ci = a.get(1);

        int offset1 = calcOffset(ci + 1, m);
        return a.get(offset1) != 0L;
    }

    public long peek() {
        final AtomicLongArray a = buffer;
        final int m = mask;
        final long ci = a.get(1);

        int offset = calcOffset(ci, m);

        long v = a.get(offset + 1);
        if (v == 0L) {
            return 0L;
        }
        return a.get(offset);
    }

    public long poll(boolean[] hasValue) {
        final AtomicLongArray a = buffer;
        final int m = mask;
        final long ci = a.get(1);

        int offset = calcOffset(ci, m);
        int offset1 = offset + 1;

        long v = a.get(offset1);
        if (v == 0L) {
            hasValue[0] = false;
            return 0L;
        }
        hasValue[0] = true;
        v = a.get(offset);
        a.lazySet(offset1, 0L);
        a.lazySet(1, ci + 2);

        return v;
    }

    public long poll() {
        final AtomicLongArray a = buffer;
        final int m = mask;
        final long ci = a.get(1);

        int offset = calcOffset(ci, m);

        long v = a.get(offset + 1);
        if (v == 0L) {
            return 0L;
        }
        v = a.get(offset);
        a.lazySet(offset + 1, 0L);
        a.lazySet(1, ci + 2);
        return v;
    }

    public boolean isEmpty() {
        final AtomicLongArray a = buffer;
        return a.get(0) == a.get(1);
    }
}
