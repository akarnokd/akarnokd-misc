package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.internal.schedulers.SingleScheduler;

public class FlowableMergeTest {
    
    static final Scheduler SCHEDULER1 = new SingleScheduler(); 

    static final Scheduler SCHEDULER2 = new SingleScheduler();  

    @Test
    public void nameLoop() throws Exception {
        for (int i = 0; i < 10000; i++) {
            if ((i % 100) == 0) {
                System.out.println(i);
            }
            name();
        }
    }
    
    @Test(timeout = 1000)
    public void name() throws Exception {
        Flowable<Integer> f1 = Flowable.<Integer>create(sink -> {
            int  i =0 ;
            while(!sink.isCancelled()) {
                sink.onNext(i++);
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(SCHEDULER1);

        Flowable<Integer> f2 = Flowable.<Integer>create(sink -> {
            int  i =0 ;
            while(!sink.isCancelled()) {
                sink.onNext(i--);
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(SCHEDULER2);

        Flowable.merge(f1,f2)
                .take(1000)
                .blockingLast();
    }
}
