package hu.akarnokd.queue;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.CyclicBarrier;

import org.junit.Test;

import hu.akarnokd.rxjava2.schedulers.Schedulers;

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
    
    @Test
    public void parallel() throws Exception {
        MpscLinkedArrayQueue<Integer> q = new MpscLinkedArrayQueue<>(16);

        int n = 100_000;
        
        for (int i = 1; i < 9; i++) {
            System.out.println("Threads: " + i);
            CyclicBarrier cb = new CyclicBarrier(i + 1);
            for (int j = 1; j <= i; j++) {
                int fj = j - 1;
                Schedulers.computation().scheduleDirect(() -> {
                    try {
                        cb.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (int k = 0; k < n; k++) {
                        q.offer(k + n * fj);
                    }
                });
            }
            
            cb.await();
            
            long t = System.currentTimeMillis();
            Set<Integer> set = new HashSet<>();
            for (int k = 0; k < n * i; k++) {
                Integer v;
                while ((v = q.poll()) == null);
                set.add(v);
            }
            assertEquals(n * i, set.size());
            assertTrue(q.isEmpty());
            System.out.println("Threads: " + i + " done @ " + (System.currentTimeMillis() - t));
        }
    }
}
