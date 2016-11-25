package hu.akarnokd.rxjava;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;

import rx.Observable;
import rx.Scheduler;
import rx.functions.*;
import rx.schedulers.Schedulers;

public class OutOfOrder {
    
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

        ExecutorService ex1 = Executors.newSingleThreadExecutor();
        Scheduler subscribeScheduler = Schedulers.from(ex1);
        ExecutorService ex2 = Executors.newSingleThreadExecutor();
        Scheduler mapScheduler = Schedulers.from(ex2);
        ExecutorService ex3 = Executors.newSingleThreadExecutor();
        Scheduler observeScheduler = Schedulers.from(ex3);

        CountDownLatch cdl = new CountDownLatch(1);

        for(int i = 0; i < total; i++){
            int j = i;
            Observable.just(null)
                    .doOnRequest(v -> System.out.println(j))
                    .subscribeOn(subscribeScheduler)
                    .observeOn(mapScheduler)
                    .map(new Func1<Object, Integer>() {
                        @Override
                        public Integer call(Object obj) {
                            int newValue = mValue.incrementAndGet();
                            mEmitValues.add(newValue);
                            return newValue;
                        }
                    })
                    .observeOn(observeScheduler)
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer value) {
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
        
        ex1.shutdown();
        ex2.shutdown();
        ex3.shutdown();
        
        System.out.println(new HashSet<>(mReceiveValues).size());
        
        Assert.assertEquals(mEmitValues, mReceiveValues);
    }
}
