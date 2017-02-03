package hu.akarnokd.rxjava2;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;


public class CacheParallel {

    @Test
    public void testParallelism() throws Exception
    {
        Flowable<Integer> flux = Flowable.just(1, 2, 3);

        Set<String> threadNames = Collections.synchronizedSet(new TreeSet<>());
        AtomicInteger count = new AtomicInteger();

        CountDownLatch latch = new CountDownLatch(3);

        flux
        // Uncomment line below for failure
        .replay(1).autoConnect()
        .parallel(3)
        .runOn(Schedulers.io())
        .doOnNext(i ->
        {
            threadNames.add(Thread.currentThread()
                    .getName());
            count.incrementAndGet();
            latch.countDown();

            tryToSleep(1000);
        })
        .sequential()
        .subscribe();

        latch.await(3, TimeUnit.SECONDS);

        Assert.assertEquals("Multithreaded count", 3, count.get());
        Assert.assertEquals("Multithreaded threads", 3, threadNames.size());
    }

    private void tryToSleep(long value) 
    {
        try
        {
            Thread.sleep(value);
        }
        catch(InterruptedException e) 
        {
            e.printStackTrace();
        }
    }
}
