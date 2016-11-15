/*
 * Copyright 2015 David Karnok
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

public final class SpscLongLinkedArrayQueue {
    final int mask;

    ALA producerBuffer;

    volatile long producerIndex;
    static final AtomicLongFieldUpdater<SpscLongLinkedArrayQueue> PRODUCER_INDEX =
            AtomicLongFieldUpdater.newUpdater(SpscLongLinkedArrayQueue.class, "producerIndex");


    ALA consumerBuffer;

    volatile long consumerIndex;
    static final AtomicLongFieldUpdater<SpscLongLinkedArrayQueue> CONSUMER_INDEX =
            AtomicLongFieldUpdater.newUpdater(SpscLongLinkedArrayQueue.class, "consumerIndex");

    static final class ALA extends AtomicLongArray {
        private static final long serialVersionUID = -505991172046651861L;

        volatile ALA next;

        static final AtomicReferenceFieldUpdater<ALA, ALA> NEXT =
                AtomicReferenceFieldUpdater.newUpdater(ALA.class, ALA.class, "next");

        ALA(int capacity) {
            super(capacity);
        }

        void soNext(ALA next) {
            NEXT.lazySet(this, next);
        }
    }

    public SpscLongLinkedArrayQueue(int capacityHint) {
        int c = Pow2.roundToPowerOfTwo(capacityHint) << 1;

        producerBuffer = consumerBuffer = new ALA(c);
        mask = c - 1;
    }

    int calcOffset(long index, int m) {
        return (int)index & m;
    }

    public boolean offer(long value) {
        final ALA a = producerBuffer;
        final int m = mask;
        final long pi = producerIndex;

        int offset3 = calcOffset(pi + 3, m);

        if (a.get(offset3) != 0L) {
            ALA b = new ALA(m + 1);
            int offset = calcOffset(pi, m);
            int offset1 = offset + 1;

            b.lazySet(offset, value);
            b.lazySet(offset1, 1);
            producerBuffer = b;
            a.soNext(b);

            a.lazySet(offset1, 2);
        } else {
            int offset = calcOffset(pi, m);
            a.lazySet(offset, value);
            a.lazySet(offset + 1, 1);
        }
        PRODUCER_INDEX.lazySet(this, pi + 2);
        return true;
    }

    public long peek(boolean[] hasValue) {
        final ALA a = consumerBuffer;
        final int m = mask;
        final long ci = consumerIndex;

        int offset = calcOffset(ci, m);

        long v = a.get(offset + 1);
        if (v == 0) {
            hasValue[0] = false;
            return 0L;
        }
        if (v == 2L) {
            ALA b = a.next;
            v = b.get(offset);
        } else {
            v = a.get(offset);
        }
        hasValue[0] = true;
        return v;
    }

    public long poll(boolean[] hasValue) {
        final ALA a = consumerBuffer;
        final int m = mask;
        final long ci = consumerIndex;

        int offset = calcOffset(ci, m);
        int offset1 = offset + 1;

        long v = a.get(offset1);

        if (v == 0L) {
            hasValue[0] = false;
            return 0;
        }

        if (v == 2) {
            ALA b = a.next;
            v = b.get(offset);
            b.lazySet(offset1, 0L);
            consumerBuffer = b;
        } else {
            v = a.get(offset);
            a.lazySet(offset1, 0L);
        }
        CONSUMER_INDEX.lazySet(this, ci + 2);

        hasValue[0] = true;
        return v;
    }

    public boolean isEmpty() {
        return producerIndex == consumerIndex;
    }
}
