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

import hu.akarnokd.rxjava2.internal.util.Pow2;

public final class SpscIntLinkedArrayQueue {
    protected long[] producerBuffer;
    
    protected volatile long producerIndex;
    static final AtomicLongFieldUpdater<SpscIntLinkedArrayQueue> PRODUCER_INDEX =
            AtomicLongFieldUpdater.newUpdater(SpscIntLinkedArrayQueue.class, "producerIndex");

    protected volatile long consumerIndex;
    static final AtomicLongFieldUpdater<SpscIntLinkedArrayQueue> CONSUMER_INDEX =
            AtomicLongFieldUpdater.newUpdater(SpscIntLinkedArrayQueue.class, "consumerIndex");
    
    protected long[] consumerBuffer;
    
    final int mask;
    
    static final int INDEX_SHIFT = 0; // 8
    
    NextNode head;
    NextNode tail;
    
    public SpscIntLinkedArrayQueue(int capacityHint) {
        int c = Pow2.roundToPowerOfTwo(Math.max(4, capacityHint));
        consumerBuffer = producerBuffer = new long[c];
        head = tail = new NextNode(null);
        this.mask = c - 1;
    }
    
    int calcOffset(long index, int mask) {
        return (int)(index << INDEX_SHIFT) & mask;
    }
    
    static final class NextNode extends AtomicReference<NextNode> {
        /** */
        private static final long serialVersionUID = 4924833193698643513L;
        final long[] buffer;
        public NextNode(long[] buffer) {
            this.buffer = buffer;
        }
    }
    
    public boolean offer(int value) {
        final int m = mask;
        final long[] a = producerBuffer;
        final long pi = producerIndex;
        
        int offset1 = calcOffset(pi + 1, m);
        
        long v = producerBuffer[offset1];

        int offset = calcOffset(pi, m);
        if ((v & 0x1_0000_0000L) != 0L) {
            long[] b = new long[m + 1];
            b[offset] = 0x1_0000_0000L | value;
            NextNode nn = new NextNode(b);
            NextNode t = tail;
            producerBuffer = b;
            tail = nn;
            t.lazySet(nn);
            a[offset] = 0x2_0000_0000L;
        } else {
            a[offset] = 0x1_0000_0000L | value;
        }
        PRODUCER_INDEX.lazySet(this, pi + 1);
        
        return true;
    }
    
    public long peek() {
        final int m = mask;
        final long[] a = consumerBuffer;
        final long ci = consumerIndex;
        
        int offset = calcOffset(ci, m);
        long v = a[offset];
        
        long hi = v & 0x3_0000_0000L;
        
        if (hi == 0L) {
            return 0;
        }
        
        if (hi == 0x2_0000_0000L) {
            NextNode nn = head.get();
            v = nn.buffer[offset];
        }
        
        return v;
    }
    
    public int peek(boolean[] hasValue) {
        long v = peek();
        hasValue[0] = (v & 0x1_0000_0000L) != 0L;
        return (int)v;
    }
    
    public int poll(boolean[] hasValue) {
        long v = poll();
        hasValue[0] = (v & 0x1_0000_0000L) != 0L;
        return (int)v;
    }

    public long poll() {
        final int m = mask;
        final long[] a = consumerBuffer;
        final long ci = consumerIndex;
        
        int offset = calcOffset(ci, m);
        long v = a[offset];
        
        long hi = v & 0x3_0000_0000L;
        
        if (hi == 0L) {
            return 0;
        }
        
        if (hi == 0x2_0000_0000L) {
            NextNode nn = head.get();
            long[] b = nn.buffer;
            v = b[offset];
            b[offset] = 0L;

            consumerBuffer = b;
            head = nn;

            CONSUMER_INDEX.lazySet(this, ci + 1);
            
            return v;
        } 

        a[offset] = 0L;
        CONSUMER_INDEX.lazySet(this, ci + 1);
        
        return v;
    }

    
    public boolean isEmpty() {
        return producerIndex == consumerIndex;
    }
    
    public void clear() {
        boolean[] b = { true };
        while (!isEmpty() && !b[0]) {
            poll(b);
        }
    }
}
