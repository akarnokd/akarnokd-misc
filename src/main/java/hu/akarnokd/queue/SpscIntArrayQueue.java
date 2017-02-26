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

import java.util.concurrent.atomic.*;

import rx.internal.util.unsafe.Pow2;

public final class SpscIntArrayQueue {
    final int mask;
    final int[] array;
    final AtomicLong producerIndex = new AtomicLong();
    final AtomicLong consumerIndex = new AtomicLong();

    int calcOffset(long index, int m) {
        return (int)index & m;
    }

    int lvElement(int[] a, int offset) {
        return a[offset];
    }

    void soElement(int[] a, int offset, int value) {
        a[offset] = value;
    }

    void soProducerIndex(long value) {
        producerIndex.lazySet(value);
    }

    long lvProducerIndex() {
        return producerIndex.get();
    }

    void soConsumerIndex(long value) {
        consumerIndex.lazySet(value);
    }

    long lvConsumerIndex() {
        return consumerIndex.get();
    }
    public SpscIntArrayQueue(int capacity) {
        int c = Pow2.roundToPowerOfTwo(capacity);
        array = new int[c];
        mask = c - 1;
    }

    public boolean offer(int value) {
        final long pi = lvProducerIndex();
        final long ci = lvConsumerIndex();
        final int m = mask;

        if (pi == ci + m + 1) {
            return false;
        }

        final int[] a = array;

        final int offset = calcOffset(pi, m);
        soElement(a, offset, value);
        soProducerIndex(pi + 1);

        return true;
    }

    public int peek(boolean[] hasValue) {
        final long pi = lvProducerIndex();
        final long ci = lvConsumerIndex();

        if (pi == ci) {
            hasValue[0] = false;
            return 0;
        }

        final int m = mask;
        final int[] a = array;

        final int offset = calcOffset(ci, m);
        hasValue[0] = true;
        return lvElement(a, offset);
    }

    public int peek() {
        final long pi = lvProducerIndex();
        final long ci = lvConsumerIndex();

        if (pi == ci) {
            return 0;
        }

        final int m = mask;
        final int[] a = array;

        final int offset = calcOffset(ci, m);
        return lvElement(a, offset);
    }


    public int poll(boolean[] hasValue) {
        final long pi = lvProducerIndex();
        final long ci = lvConsumerIndex();

        if (pi == ci) {
            hasValue[0] = false;
            return 0;
        }

        final int m = mask;
        final int[] a = array;

        final int offset = calcOffset(ci, m);
        int lvElement = lvElement(a, offset);
        soConsumerIndex(ci + 1);

        hasValue[0] = true;
        return lvElement;
    }

    public int poll() {
        final long pi = lvProducerIndex();
        final long ci = lvConsumerIndex();

        if (pi == ci) {
            return 0;
        }

        final int m = mask;
        final int[] a = array;

        final int offset = calcOffset(ci, m);
        int lvElement = lvElement(a, offset);
        soConsumerIndex(ci + 1);

        return lvElement;
    }

    public boolean isEmpty() {
        return lvProducerIndex() == lvConsumerIndex();
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
        long ci = lvConsumerIndex();

        for (;;) {
            long pi = lvProducerIndex();
            long ci2 = lvConsumerIndex();
            if (ci2 == ci) {
                return (int)((pi - ci) >> 1);
            }
            ci = ci2;
        }
    }
}
