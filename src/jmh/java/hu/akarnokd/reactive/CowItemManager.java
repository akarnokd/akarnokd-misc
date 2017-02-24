package hu.akarnokd.reactive;

import java.util.concurrent.atomic.AtomicReference;

public class CowItemManager<T> extends AtomicReference<Object[]>{

    private static final long serialVersionUID = 873320459854565358L;

    static final Object[] EMPTY = new Object[0];
    static final Object[] TERMINATED = new Object[0];
    
    public boolean offer(T item) {
        for (;;) {
            Object[] a = get();
            if (a == TERMINATED) {
                return false;
            }
            int n = a.length;
            Object[] b = new Object[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = item;
            if (compareAndSet(a, b)) {
                return true;
            }
        }
    }
    
    public void remove(T item) {
        for (;;) {
            Object[] a = get();
            int n = a.length;
            if (n == 0) {
                break;
            }
            
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == item) {
                    j = i;
                    break;
                }
            }
            if (j < 0) {
                break;
            }
            Object[] b;
            if (n == 0) {
                b = EMPTY;
            } else {
                b = new Object[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (compareAndSet(a, b)) {
                break;
            }
        }
    }

    public void clear() {
        getAndSet(EMPTY);
    }

    @SuppressWarnings("unchecked")
    public void close() {
        for (Object o : getAndSet(TERMINATED)) {
            onRemove((T)o);
        }
    }
    
    public void onRemove(T item) {
        
    }
}
