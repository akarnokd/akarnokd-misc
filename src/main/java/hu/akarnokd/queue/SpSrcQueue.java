package hu.akarnokd.queue;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

import hu.akarnokd.rxjava2.internal.util.Pow2;

/**
 * Single producer, single random consumer queue.
 * 
 * @param <T> the value type
 */
public class SpSrcQueue<T> implements Iterable<T>, Iterator<T> {

    final int mask;
    
    long producerIndex;
    ARA<T> producerArray;
    
    long consumerIndex;
    ARA<T> consumerArray;
    
    long iteratorIndex;
    ARA<T> iteratorArray;
    
    volatile boolean cancelled;
    
    static final Object NEXT = new Object();

    static final Object TOMBSTONE = new Object();

    public SpSrcQueue(int capacity) {
        mask = Pow2.roundToPowerOfTwo(capacity);
        producerArray = consumerArray = new ARA<>(mask + 1);
    }
    
    public boolean add(T e) {
        ARA<T> pa = producerArray;
        long pi = producerIndex;
        int m = mask;
        int offset = (int)pi;
        
        if (pa.lvElement(offset) == null) {
            producerIndex = pi + 1;
            pa.soElement(offset, e);
        } else {
            ARA<T> next = new ARA<>(m + 1);
            
            producerIndex = pi + 1;
            producerArray = next;
            next.soElement(offset, e);
            pa.soNext(next);
            pa.soElementNext(offset, NEXT);
        }
        
        return cancelled;
    }

    public void remove(T e) {
        long idx = consumerIndex;
        int m = mask;
        ARA<T> ca = consumerArray;
        
        for (;;) {
            if (ca.remaining == 0) {
                ca = ca.lvNext();
                continue;
            }
            int offset = (int)idx & m;
            Object o = ca.lvElement(offset);
            
            if (o == NEXT) {
                ca = ca.lvNext();
                continue;
            }
            
            if (o == null) {
                break;
            }
            if (o == e) {
                ca.soElementNext(offset, TOMBSTONE);
                ca.remaining--;
                break;
            }
        }
        
        ca = consumerArray;
        
        while (ca.remaining == 0 && ca.lvNext() != null) {
            ca = ca.lvNext();
        }
        consumerArray = ca;
    }
    
    public void cancel() {
        cancelled = true;
    }
    
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public T next() {
        // TODO Auto-generated method stub
        return null;
    }
    
    static final class ARA<E> extends AtomicReferenceArray<Object> {

        /** */
        private static final long serialVersionUID = 6887678375436293958L;

        final int nextOffset;
        
        int remaining;
        
        public ARA(int length) {
            super(length + 1);
            nextOffset = length;
            remaining = length;
        }

        public void soElement(int offset, E e) {
            lazySet(offset, e);
        }
        
        @SuppressWarnings("unchecked")
        public E lvElement(int offset) {
            return (E)get(offset);
        }
        
        @SuppressWarnings("unchecked")
        public ARA<E> lvNext() {
            return (ARA<E>)get(nextOffset);
        }
        
        public void soNext(ARA<E> next) {
            lazySet(nextOffset, next);
        }
        
        public void soElementNext(int offset, Object o) {
            lazySet(offset, o);
        }
    }
}
