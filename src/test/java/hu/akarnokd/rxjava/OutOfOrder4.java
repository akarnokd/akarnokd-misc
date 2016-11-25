package hu.akarnokd.rxjava;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;

import io.reactivex.*;
import io.reactivex.functions.*;
import io.reactivex.internal.schedulers.SingleScheduler;

public class OutOfOrder4 {
    
    @Test
    public void loop() throws Exception {
        for (int i = 0; i < 1000; i++) {
            test();
        }
    }
    
    @Test
    public void test() throws Exception {
        final int total = 1000;
        AtomicInteger mValue = new AtomicInteger(0);
        AtomicInteger mCount = new AtomicInteger(0);
        List<Integer> mEmitValues = new ArrayList<>();
        List<Integer> mReceiveValues = new ArrayList<>();

        Scheduler subscribeScheduler = new SingleScheduler();
        Scheduler mapScheduler = new SingleScheduler();
        Scheduler observeScheduler = new SingleScheduler();

        CountDownLatch cdl = new CountDownLatch(1);

        {
            Flowable.range(1, 1000).hide()
                    .subscribeOn(subscribeScheduler)
                    .observeOn(mapScheduler)
                    .map(new Function<Object, Integer>() {
                        @Override
                        public Integer apply(Object obj) {
                            int newValue = mValue.incrementAndGet();
                            mEmitValues.add(newValue);
                            return newValue;
                        }
                    })
                    .observeOn(observeScheduler)
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer value) {
                            mReceiveValues.add(value);
                            if(mCount.incrementAndGet() == total) {
                                try {
                                    System.out.println("run complete");
                                    for (int i = 0; i < total; i++) {
                                        if (!mEmitValues.get(i).equals(mReceiveValues.get(i))) {
                                            System.out.println(i + ": " + mEmitValues.get(i) + " vs " + mReceiveValues.get(i));
                                        }
                                    }
                                } finally {
                                    cdl.countDown();
                                }
                                
                            }
                        }
                    });
        }

        cdl.await();
        
        Thread.sleep(100);

        subscribeScheduler.shutdown();
        mapScheduler.shutdown();
        observeScheduler.shutdown();
        
        System.out.println(new HashSet<>(mReceiveValues).size());
        
        Assert.assertEquals(mEmitValues, mReceiveValues);
    }
}
