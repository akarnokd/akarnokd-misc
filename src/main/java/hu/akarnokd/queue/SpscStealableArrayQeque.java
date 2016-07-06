package hu.akarnokd.queue;

import java.util.concurrent.atomic.*;

import io.reactivex.internal.util.Pow2;


public final class SpscStealableArrayQeque<T> extends AtomicReferenceArray<Object> {
    /** */
    private static final long serialVersionUID = -806326314905613274L;

    volatile long producerIndex;
    @SuppressWarnings("rawtypes")
    static final AtomicLongFieldUpdater<SpscStealableArrayQeque> PRODUCER_INDEX =
            AtomicLongFieldUpdater.newUpdater(SpscStealableArrayQeque.class, "producerIndex");
    
    volatile long consumerIndex;
    @SuppressWarnings("rawtypes")
    static final AtomicLongFieldUpdater<SpscStealableArrayQeque> CONSUMER_INDEX =
            AtomicLongFieldUpdater.newUpdater(SpscStealableArrayQeque.class, "consumerIndex");
    
    final int mask;
    
    static final Object STOLEN = new Object();
    
    public SpscStealableArrayQeque(int capacity) {
        super(Pow2.roundToPowerOfTwo(capacity));
        this.mask = length() - 1;
    }
    
    public boolean offer(T value) {
        final int m = mask;
        long pi = producerIndex;
        
        int offset = (int)pi & m;
        
        Object o = get(offset);
        if (o == null || o == STOLEN) {
            lazySet(offset, value);
            PRODUCER_INDEX.lazySet(this, pi + 1);
            return true;
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public T poll() {
        final int m = mask;
        final long ci = consumerIndex;
        long ci1 = ci;
        
        for (;;) {
            int offset = (int)ci1 & m;
            
            Object o = get(offset);
            if (o == null) {
                if (ci1 != ci) {
                    CONSUMER_INDEX.lazySet(this, ci1);
                }
                return null;
            } else
            if (o == STOLEN) {
                ci1++;
            } else {
                if (compareAndSet(offset, o, null)) {
                    CONSUMER_INDEX.lazySet(this, ci1 + 1);
                    return (T)o;
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public T steal() {
        final int m = mask;

        long pi = producerIndex;

        for (;;) {
            
            int offset = (int)pi & m;
            
            Object o = get(offset);
            if (o == null) {
                return null;
            }
            if (o == STOLEN) {
                pi--;
                continue;
            }
            if (compareAndSet(offset, o, STOLEN)) {
                return (T)o;
            }
        }
    }
}
