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

import java.util.concurrent.atomic.AtomicLong;

import rx.internal.util.unsafe.Pow2;

public final class SpscIntArrayQueueAtomic {
    final int mask;
    final long[] array;
    final AtomicLong producerIndex = new AtomicLong();
    final AtomicLong consumerIndex = new AtomicLong();

    int calcOffset(long index, int m) {
        return (int)index & m;
    }

    long lvElement(long[] a, int offset) {
        return a[offset];
    }

    void soElement(long[] a, int offset, long value) {
        a[offset] = value;
    }

    void soProducerIndex(long value) {
        producerIndex.lazySet(value);
    }

    long lpProducerIndex() {
        return producerIndex.get();
    }

    void soConsumerIndex(long value) {
        consumerIndex.lazySet(value);
    }

    long lpConsumerIndex() {
        return consumerIndex.get();
    }
    public SpscIntArrayQueueAtomic(int capacity) {
        int c = Pow2.roundToPowerOfTwo(capacity);
        array = new long[c];
        mask = c - 1;
    }

    public boolean offer(int value) {
        final int m = mask;
        final long[] a = array;
        final long pi = lpProducerIndex();

        final int offset = calcOffset(pi, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            return false;
        }
        soElement(a, offset, value | 0x1_0000_0000L);
        soProducerIndex(pi + 1);

        return true;
    }

    public int peek(boolean[] hasValue) {
        final int m = mask;
        final long[] a = array;
        final long ci = lpConsumerIndex();

        final int offset = calcOffset(ci, m);
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
        final long ci = lpConsumerIndex();

        final int offset = calcOffset(ci, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            return (int)v;
        }
        return 0;
    }


    public int poll(boolean[] hasValue) {
        final int m = mask;
        final long[] a = array;
        final long ci = lpConsumerIndex();

        final int offset = calcOffset(ci, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            soElement(a, offset, 0L);
            soConsumerIndex(ci + 1);
            hasValue[0] = true;
            return (int)v;
        }
        hasValue[0] = false;
        return 0;
    }

    public int poll() {
        final int m = mask;
        final long[] a = array;
        final long ci = lpConsumerIndex();

        final int offset = calcOffset(ci, m);
        final long v = lvElement(a, offset);
        if (v != 0L) {
            soElement(a, offset, 0L);
            soConsumerIndex(ci + 1);
            return (int)v;
        }
        return 0;
    }

    public boolean isEmpty() {
        return lpProducerIndex() == lpConsumerIndex();
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
        long ci = lpConsumerIndex();

        for (;;) {
            long pi = lpProducerIndex();
            long ci2 = lpConsumerIndex();
            if (ci2 == ci) {
                return (int)((pi - ci) >> 1);
            }
            ci = ci2;
        }
    }
}
