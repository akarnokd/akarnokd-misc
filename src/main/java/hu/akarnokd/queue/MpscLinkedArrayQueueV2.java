package hu.akarnokd.queue;

import java.util.Objects;
import java.util.concurrent.atomic.*;


abstract class Cold {
    final int size;

    Cold(int size) {
        this.size = size;
    }
}

abstract class Pad1 extends Cold {
    volatile long p0, p1, p2, p3, p4, p5, p6;
    volatile long q1, q2, q3, q4, q5, q6, q7, q8;

    Pad1(int size) {
        super(size);
    }
}

abstract class Tail<E> extends Pad1 {

    volatile Node<E> tail;
    @SuppressWarnings("rawtypes")
    static final AtomicReferenceFieldUpdater<Tail, Node> TAIL =
            AtomicReferenceFieldUpdater.newUpdater(Tail.class, Node.class, "tail");

    Tail(int size) {
        super(size);
    }
}
abstract class Pad2<E> extends Tail<E> {
    volatile long p0, p1, p2, p3, p4, p5, p6;
    volatile long q1, q2, q3, q4, q5, q6, q7, q8;

    Pad2(int size) {
        super(size);
    }
}

abstract class Head<E> extends Pad2<E> {
    Node<E> head;
    int pollIndex;

    Head(int size) {
        super(size);
    }
}

abstract class Pad3<E> extends Head<E> {
    volatile long p0, p1, p2, p3, p4, p5, p6;
    volatile long q1, q2, q3, q4, q5, q6, q7, q8;

    Pad3(int size) {
        super(size);
    }

}

public final class MpscLinkedArrayQueueV2<E> extends Pad3<E> {

    static final int SHIFT = 0;

    public MpscLinkedArrayQueueV2(int size) {
        super(size);
        Node<E> start = new Node<>(size);
        head = start;
        TAIL.lazySet(this, start);
    }

    public void enqueue(E item) {
        Objects.requireNonNull(item, "item is null");
        int s = size;
        Node<E> t = tail;
        for (;;) {
            int offset = t.offer();

            if (offset >= s) {
                Node<E> n = new Node<E>(s, item);
                if (t.casNext(n)) {
                    TAIL.compareAndSet(this, t, n);
                    return;
                }
                n = t.next;
                TAIL.compareAndSet(this, t, n);
                t = n;
                continue;
            }

            t.lazySet(offset << SHIFT, item);
            return;
        }
    }

    public E peek() {
        Node<E> h = head;
        int s = size;
        int offset = pollIndex;

        if (s == offset) {
            Node<E> n = h.next;
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
            if (h.offerIndex == offset) {
                return null;
            }
        }
    }

    public E dequeue() {
        Node<E> h = head;
        int s = size;
        int offset = pollIndex;

        if (s == offset) {
            Node<E> n = h.next;
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
            if (h.offerIndex == offset) {
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
        Node<E> h = head;
        return h.offerIndex == pollIndex && h.next == null;
    }
}

final class Node<E> extends AtomicReferenceArray<E> {
    private static final long serialVersionUID = 841157748592449297L;

    volatile int offerIndex;
    @SuppressWarnings("rawtypes")
    static final AtomicIntegerFieldUpdater<Node> OFFER_INDEX =
            AtomicIntegerFieldUpdater.newUpdater(Node.class, "offerIndex");

    volatile Node<E> next;
    @SuppressWarnings("rawtypes")
    static final AtomicReferenceFieldUpdater<Node, Node> NEXT =
            AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");

    Node(int itemCount) {
        super(itemCount << MpscLinkedArrayQueueV2.SHIFT);
    }

    Node(int itemCount, E first) {
        this(itemCount << MpscLinkedArrayQueueV2.SHIFT);
        lazySet(0, first);
        OFFER_INDEX.lazySet(this, 1);
    }

    boolean casNext(Node<E> next) {
        return NEXT.compareAndSet(this, null, next);
    }

    int offer() {
        return OFFER_INDEX.getAndIncrement(this);
    }
}


//final class Node<E> extends AtomicReferenceArray<Object> {
//    private static final long serialVersionUID = 841157748592449297L;
//
//    final AtomicInteger offerIndex;
//
//    final AtomicReference<Node<E>> next;
//
//    int pollIndex;
//
//    Node(int itemCount) {
//        super(itemCount);
//        this.offerIndex = new AtomicInteger();
//        this.next = new AtomicReference<>();
//    }
//
//    Node(int itemCount, E first) {
//        this(itemCount);
//        lazySet(0, first);
//        offerIndex.lazySet(1);
//    }
//
//    @SuppressWarnings("unchecked")
//    E lvItem(int index) {
//        return (E)get(index);
//    }
//
//    void soItem(int index, E item) {
//        lazySet(index, item);
//    }
//
//    Node<E> lvNext(int size) {
//        return next.get();
//    }
//
//    boolean casNext(Node<E> next, int size) {
//        return this.next.compareAndSet(null, next);
//    }
//}
