package hu.akarnokd.queue;

import java.util.Objects;
import java.util.concurrent.atomic.*;

public final class MpscLinkedArrayQueue<T> {
    
    volatile ARA producerBuffer;
    @SuppressWarnings("rawtypes")
    static final AtomicReferenceFieldUpdater<MpscLinkedArrayQueue, ARA> PRODUCER_BUFFER =
            AtomicReferenceFieldUpdater.newUpdater(MpscLinkedArrayQueue.class, ARA.class, "producerBuffer");
    
    volatile long producerIndex;
    @SuppressWarnings("rawtypes")
    static final AtomicLongFieldUpdater<MpscLinkedArrayQueue> PRODUCER_INDEX =
            AtomicLongFieldUpdater.newUpdater(MpscLinkedArrayQueue.class, "producerIndex");
    
    ARA consumerBuffer;
    volatile long consumerIndex;
    @SuppressWarnings("rawtypes")
    static final AtomicLongFieldUpdater<MpscLinkedArrayQueue> CONSUMER_INDEX =
            AtomicLongFieldUpdater.newUpdater(MpscLinkedArrayQueue.class, "consumerIndex");

    static final ARA ALLOCATING = new ARA(0, 0L);
    
    final int capacity;
    
    public MpscLinkedArrayQueue(int capacity) {
        this.capacity = capacity;
        ARA a = new ARA(capacity, 0L);
        consumerBuffer = a;
        PRODUCER_BUFFER.lazySet(this, a);
    }
    
    public boolean offer(T value) {
        Objects.requireNonNull(value);
        final int c = capacity;
        
        ARA a = producerBuffer;

        long idx = PRODUCER_INDEX.getAndIncrement(this);
        
        long st = a.start;
        if (st <= idx && idx < a.end) {
            a.lazySet((int)(idx - st), value);
            return true;
        }
        for (;;) {
            ARA b = (ARA)a.get(c);
            if (b == null) {
                if (a.compareAndSet(c, b, ALLOCATING)) {
                    b = new ARA(c, st + c);
                    b.lazySet(0, value);
                    a.lazySet(c, b);
                    return true;
                } else {
                    while ((b = (ARA)a.get(c)) == ALLOCATING);
                }
            } else
            if (b == ALLOCATING) {
                while ((b = (ARA)a.get(c)) == ALLOCATING);
            }
            if (producerBuffer == a) {
                PRODUCER_BUFFER.compareAndSet(this, a, b);
            }
            st += c;
            if (st <= idx && idx < b.end) {
                b.lazySet((int)(idx - st), value);
                return true;
            }
            a = b;
        }
    }
    
    public T poll() {
        final ARA a = consumerBuffer;
        final long ci = consumerIndex;
        final int c = capacity;
        
        long st = a.start;
        long en = a.end;
        
        if (ci < en) {
            int offset = (int)(ci - st);
            @SuppressWarnings("unchecked")
            T v = (T)a.get(offset);
            if (v != null) {
                a.lazySet(offset, null);
                CONSUMER_INDEX.lazySet(this, ci + 1);
            }
            return v;
        }
        ARA b;
        while ((b = (ARA)a.get(c)) == ALLOCATING);
        consumerBuffer = b;
        int offset = (int)(ci - en);
        @SuppressWarnings("unchecked")
        T v = (T)b.get(offset);
        if (v != null) {
            b.lazySet(offset, null);
            CONSUMER_INDEX.lazySet(this, ci + 1);
        }
        return v;
    }
    
    public T peek() {
        final ARA a = consumerBuffer;
        final long ci = consumerIndex;
        final int c = capacity;
        
        long st = a.start;
        long en = a.end;
        
        if (ci < en) {
            int offset = (int)(ci - st);
            @SuppressWarnings("unchecked")
            T v = (T)a.get(offset);
            return v;
        }
        ARA b;
        while ((b = (ARA)a.get(c)) == ALLOCATING);
        int offset = (int)(ci - en);
        @SuppressWarnings("unchecked")
        T v = (T)b.get(offset);
        return v;
    }
    
    public int size() {
        long pi = producerIndex;
        for (;;) {
            long ci = consumerIndex;
            long pi2 = producerIndex;
            if (pi == pi2) {
                return (int)(pi - ci);
            }
            pi = pi2;
        }
    }
    
    public boolean isEmpty() {
        return producerIndex == consumerIndex;
    }
    
    public void clear() {
        while (poll() != null && !isEmpty());
    }
    
    
    static final class ARA extends AtomicReferenceArray<Object> {
        /** */
        private static final long serialVersionUID = 8682709058175629817L;

        final long start;
        final long end;
        final int itemCount;
        
        public ARA(int capacity, long start) {
            super(capacity + 1);
            this.start = start;
            this.itemCount = capacity;
            this.end = start + capacity;
        }
    }
}
