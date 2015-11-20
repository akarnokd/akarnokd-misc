package hu.akarnokd.queue;

import org.junit.Test;
import static org.junit.Assert.*;

public class MpscLinkedArrayQueueTest {
    @Test
    public void simple() {
        MpscLinkedArrayQueue<Integer> q = new MpscLinkedArrayQueue<>(16);
        
        for (int i = 0; i < 1000; i++) {
            assertTrue(q.offer(i));
            assertFalse(q.isEmpty());
            assertEquals((Integer)i, q.peek());
            assertEquals((Integer)i, q.poll());
            assertTrue(q.isEmpty());
        }
        
        for (int i = 0; i < 1000; i++) {
            assertTrue(q.offer(i));
        }
        assertFalse(q.isEmpty());
        
        for (int i = 0; i < 1000; i++) {
            assertFalse(q.isEmpty());
            assertEquals((Integer)i, q.peek());
            assertEquals((Integer)i, q.poll());
        }
        
        assertTrue(q.isEmpty());
    }
}
