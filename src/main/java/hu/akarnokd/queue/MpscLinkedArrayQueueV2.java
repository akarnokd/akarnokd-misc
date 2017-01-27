package hu.akarnokd.queue;

import java.util.Objects;
import java.util.concurrent.atomic.*;

abstract class ColdV2<E> {
    final int size;

    final AtomicReference<NodeV2<E>> tail;

    ColdV2(int size) {
        this.size = size;
        this.tail = new AtomicReference<>();
    }
}

abstract class Pad1V2<E> extends ColdV2<E> {
    volatile long p0, p1, p2, p3, p4, p5, p6;
    volatile long q1, q2, q3, q4, q5, q6, q7, q8;

    Pad1V2(int size) {
        super(size);
    }
}

abstract class HeadV2<E> extends Pad1V2<E> {
    NodeV2<E> head;
    int pollIndex;

    HeadV2(int size) {
        super(size);
    }
}

abstract class Pad3V2<E> extends HeadV2<E> {
    volatile long p0, p1, p2, p3, p4, p5, p6;
    volatile long q1, q2, q3, q4, q5, q6, q7, q8;

    Pad3V2(int size) {
        super(size);
    }

}

public final class MpscLinkedArrayQueueV2<E> extends Pad3V2<E> {

    static final int SHIFT = 0;

    public MpscLinkedArrayQueueV2(int size) {
        super(size);
        NodeV2<E> start = new NodeV2<>(size);
        head = start;
        tail.lazySet(start);
    }

    public void enqueue(E item) {
        Objects.requireNonNull(item, "item is null");
        int s = size;
        AtomicReference<NodeV2<E>> ftail = tail;
        NodeV2<E> t = ftail.get();
        for (;;) {
            int offset = t.offer();

            if (offset >= s) {
                NodeV2<E> n = new NodeV2<>(s, item);
                if (t.casNext(n)) {
                    ftail.compareAndSet(t, n);
                    return;
                }
                n = t.next.get();
                ftail.compareAndSet(t, n);
                t = n;
                continue;
            }

            t.lazySet(offset << SHIFT, item);
            return;
        }
    }

    public E peek() {
        NodeV2<E> h = head;
        int s = size;
        int offset = pollIndex;

        if (s == offset) {
            NodeV2<E> n = h.next.get();
            if (n == null) {
                return null;
            }

            E item = n.get(0);
            head = n;
            return item;
        }

        int idx = offset << SHIFT;
        for (;;) {
            E item = h.get(idx);
            if (item != null) {
                return item;
            }
            if (h.offerIndex.get() == offset) {
                return null;
            }
        }
    }

    public E dequeue() {
        NodeV2<E> h = head;
        int s = size;
        int offset = pollIndex;

        if (s == offset) {
            NodeV2<E> n = h.next.get();
            if (n == null) {
                return null;
            }

            E item = n.get(0);
            n.lazySet(0, null);

            pollIndex = 1;
            head = n;
            return item;
        }

        int idx = offset << SHIFT;

        for (;;) {
            E item = h.get(idx);
            if (item != null) {
                h.lazySet(idx, null);
                pollIndex = offset + 1;
                return item;
            }
            if (h.offerIndex.get() == offset) {
                return null;
            }
        }
    }

    public boolean offer(E item) {
        enqueue(item);
        return true;
    }

    public E poll() {
        return dequeue();
    }

    public boolean isEmpty() {
        NodeV2<E> h = head;
        return h.offerIndex.get() == pollIndex && h.next == null;
    }
}

final class NodeV2<E> extends AtomicReferenceArray<E> {
    private static final long serialVersionUID = 841157748592449297L;

    final AtomicInteger offerIndex;

    final AtomicReference<NodeV2<E>> next;

    NodeV2(int itemCount) {
        super(itemCount << MpscLinkedArrayQueueV2.SHIFT);
        offerIndex = new AtomicInteger();
        next = new AtomicReference<>();
    }

    NodeV2(int itemCount, E first) {
        this(itemCount << MpscLinkedArrayQueueV2.SHIFT);
        lazySet(0, first);
        offerIndex.lazySet(1);
    }

    boolean casNext(NodeV2<E> next) {
        return this.next.compareAndSet(null, next);
    }

    int offer() {
        return offerIndex.getAndIncrement();
    }
}
