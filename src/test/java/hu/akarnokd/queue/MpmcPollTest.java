package hu.akarnokd.queue;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.jctools.queues.MpmcArrayQueue;
import org.jctools.queues.atomic.MpmcAtomicArrayQueue;
import org.junit.Test;

import io.reactivex.exceptions.CompositeException;
import io.reactivex.internal.util.ExceptionHelper;

public class MpmcPollTest {

    ExecutorService exec = Executors.newSingleThreadExecutor();

    @Test
    public void atomic() {
        for (int i = 0; i < 100000; i++) {
            MpmcAtomicArrayQueue<Integer> q = new MpmcAtomicArrayQueue<>(128);
            
            q.offer(1);
            
            Integer[] result = new Integer[] { 2, 2 };

            race(() -> {
                result[0] = q.poll();
            }, () -> {
                result[1] = q.poll();
            }, exec);
            
            assertTrue(Arrays.toString(result), (result[0] == null && result[1] == 1) || (result[0] == 1 && result[1] == null));
        }
    }

    @Test
    public void unsafe() {
        for (int i = 0; i < 100000; i++) {
            MpmcArrayQueue<Integer> q = new MpmcArrayQueue<>(128);
            
            q.offer(1);
            
            Integer[] result = new Integer[] { 2, 2 };

            race(() -> {
                result[0] = q.poll();
            }, () -> {
                result[1] = q.poll();
            }, exec);
            
            assertTrue(Arrays.toString(result), (result[0] == null && result[1] == 1) || (result[0] == 1 && result[1] == null));
        }
    }

    public static void race(final Runnable r1, final Runnable r2, ExecutorService s) {
        final AtomicInteger count = new AtomicInteger(2);
        final CountDownLatch cdl = new CountDownLatch(2);

        final Throwable[] errors = { null, null };

        s.submit(new Runnable() {
            @Override
            public void run() {
                if (count.decrementAndGet() != 0) {
                    while (count.get() != 0) { }
                }

                try {
                    try {
                        r1.run();
                    } catch (Throwable ex) {
                        errors[0] = ex;
                    }
                } finally {
                    cdl.countDown();
                }
            }
        });

        if (count.decrementAndGet() != 0) {
            while (count.get() != 0) { }
        }

        try {
            try {
                r2.run();
            } catch (Throwable ex) {
                errors[1] = ex;
            }
        } finally {
            cdl.countDown();
        }

        try {
            if (!cdl.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("The wait timed out!");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        if (errors[0] != null && errors[1] == null) {
            throw ExceptionHelper.wrapOrThrow(errors[0]);
        }

        if (errors[0] == null && errors[1] != null) {
            throw ExceptionHelper.wrapOrThrow(errors[1]);
        }

        if (errors[0] != null && errors[1] != null) {
            throw new CompositeException(errors);
        }
    }
}
