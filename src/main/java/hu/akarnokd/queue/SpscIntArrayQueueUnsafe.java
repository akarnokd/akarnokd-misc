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

import static rx.internal.util.unsafe.UnsafeAccess.*;

import rx.internal.util.unsafe.Pow2;

public final class SpscIntArrayQueueUnsafe {

    final int mask;
    final long[] array;
    volatile long producerIndex;
    volatile long consumerIndex;

    static final long PI = addressOf(SpscIntArrayQueueUnsafe.class, "producerIndex");
    static final long CI = addressOf(SpscIntArrayQueueUnsafe.class, "consumerIndex");

    static final long ARRAY_INDEX_BASE;
    static final long ARRAY_INDEX_SHIFT;

    static {
        ARRAY_INDEX_BASE = UNSAFE.arrayBaseOffset(long[].class);
        ARRAY_INDEX_SHIFT = 3;
    }

    long calcOffset(long index, int m) {
        return ARRAY_INDEX_BASE + ((index & m) << ARRAY_INDEX_SHIFT);
    }

    long lvElement(long[] a, long offset) {
        return UNSAFE.getLongVolatile(a, offset);
    }

    void soElement(long[] a, long offset, long value) {
        UNSAFE.putOrderedLong(a, offset, value);
    }

    void soProducerIndex(long value) {
        UNSAFE.putOrderedLong(this, PI, value);
    }

    void soConsumerIndex(long value) {
        UNSAFE.putOrderedLong(this, CI, value);
    }

    public SpscIntArrayQueueUnsafe(int capacity) {
        int c = Pow2.roundToPowerOfTwo(capacity);
        array = new long[c];
        mask = c - 1;
    }

    public boolean offer(int value) {
        final int m = mask;
        final long[] a = array;
        final long pi = producerIndex;

        final long offset = calcOffset(pi, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            return false;
        }
        soProducerIndex(pi + 1);
        soElement(a, offset, value | 0x1_0000_0000L);

        return true;
    }

    public int peek(boolean[] hasValue) {
        final int m = mask;
        final long[] a = array;
        final long ci = consumerIndex;

        final long offset = calcOffset(ci, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            hasValue[0] = true;
            return (int)v;
        }
        hasValue[0] = false;
        return 0;
    }

    public int peek() {
        final int m = mask;
        final long[] a = array;
        final long ci = consumerIndex;

        final long offset = calcOffset(ci, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            return (int)v;
        }
        return 0;
    }


    public int poll(boolean[] hasValue) {
        final int m = mask;
        final long[] a = array;
        final long ci = consumerIndex;

        final long offset = calcOffset(ci, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            soConsumerIndex(ci + 1);
            soElement(a, offset, 0L);
            hasValue[0] = true;
            return (int)v;
        }
        hasValue[0] = false;
        return 0;
    }

    public int poll() {
        final int m = mask;
        final long[] a = array;
        final long ci = consumerIndex;

        final long offset = calcOffset(ci, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            soConsumerIndex(ci + 1);
            soElement(a, offset, 0L);
            return (int)v;
        }
        return 0;
    }

    public boolean isEmpty() {
        return producerIndex == consumerIndex;
    }

    public boolean hasValue() {
        return !isEmpty();
    }

    public void clear() {
        while (hasValue()) {
            poll();
        }
    }

    public int size() {
        long ci = consumerIndex;

        for (;;) {
            long pi = producerIndex;
            long ci2 = consumerIndex;
            if (ci2 == ci) {
                return (int)((pi - ci) >> 1);
            }
            ci = ci2;
        }
    }
}
