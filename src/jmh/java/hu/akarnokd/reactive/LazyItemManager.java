package hu.akarnokd.reactive;

import java.util.concurrent.atomic.*;

public class LazyItemManager<T> extends AtomicReference<AtomicReferenceArray<T>> {

    private static final long serialVersionUID = 3395528110040140227L;

    @SuppressWarnings("rawtypes")
    static final AtomicReferenceArray TERMINATED = new AtomicReferenceArray(0);

    public LazyItemManager(int capacity) {
        lazySet(new AtomicReferenceArray<T>(capacity));
    }

    public boolean offer(T item) {
        AtomicReferenceArray<T> a = get();
        if (a != TERMINATED) {
            int n = a.length();
            for (int i = 0; i < n; i++) {
                if (a.get(i) == null) {
                    a.set(i, item);
                    if (get() == TERMINATED) {
                        onRemove(item);
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void remove(T item) {
        AtomicReferenceArray<T> a = get();
        int n = a.length();
        for (int i = 0; i < n; i++) {
            if (a.get(i) == item) {
                a.lazySet(i, null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void close() {
        AtomicReferenceArray<T> a = get();
        if (a != TERMINATED) {
            a = getAndSet(TERMINATED);
            if (a != TERMINATED) {
                int n = a.length();
                for (int i = 0; i < n; i++) {
                    T t = a.get(i);
                    if (t != null) {
                        onRemove(t);
                    }
                }
            }
        }
    }

    public void clear() {
        AtomicReferenceArray<T> a = get();
        int n = a.length();
        for (int i = 0; i < n; i++) {
            a.lazySet(i, null);
        }
    }

    public void onRemove(T item) {

    }
}
