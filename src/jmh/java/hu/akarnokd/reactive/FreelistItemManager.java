package hu.akarnokd.reactive;

import java.util.concurrent.atomic.*;

import rsc.util.PowerOf2;

public class FreelistItemManager<T> extends AtomicReferenceArray<IndexedItem<T>>{

    /** */
    private static final long serialVersionUID = 262938802448837852L;

    volatile boolean closed;
    
    final AtomicIntegerArray freelist;
    
    public FreelistItemManager(int capacity) {
        super(powerOf2(capacity));
        freelist = new AtomicIntegerArray(length() + 2);
    }
    
    static int powerOf2(int x) {
        return PowerOf2.roundUp(x);
    }
    
    public IndexedItem<T> offer(T item) {
        if (!closed) {
            int ci = lvConsumerIndex();
            int idx = freelist.get(ci);
            if (idx == 0) {
                idx = ci + 1;
            }
            IndexedItem<T> iitem = new IndexedItem<>(idx, item);
            lazySet(idx - 1, iitem);
            soConsumerIndex(ci + 1, length());
            if (closed) {
                lazySet(idx - 1, null);
            } else {
                return iitem;
            }
        }
        return null;
    }
    
    public void remove(IndexedItem<T> iitem) {
        int idx = iitem.index;
        lazySet(idx - 1, null);
        int pi = lvProducerIndex();
        freelist.lazySet(pi, idx);
        soProducerIndex(pi + 1, length());
    }
    
    public void close() {
        closed = true;
        int m = length();
        for (int i = 0; i < m; i++) {
            IndexedItem<T> iitem = get(i);
            lazySet(i, null);
            onRemove(iitem.item);
        }
    }

    public void onRemove(T item) {
        
    }
    
    int lvProducerIndex() {
        return freelist.get(length());
    }
    int lvConsumerIndex() {
        return freelist.get(length() + 1);
    }
    
    void soProducerIndex(int idx, int m) {
        freelist.lazySet(m, idx & (m - 1));
    }
    void soConsumerIndex(int idx, int m) {
        freelist.lazySet(m + 1, idx & (m - 1));
    }
}

final class IndexedItem<T> {
    public final int index;
    public final T item;
    public IndexedItem(int index, T item) {
        this.index = index;
        this.item = item;
    }
}
