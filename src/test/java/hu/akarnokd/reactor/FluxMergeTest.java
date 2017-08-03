package hu.akarnokd.reactor;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.*;

public class FluxMergeTest {
    
    static final Scheduler SCHEDULER1 = Schedulers.newSingle("A"); 

    static final Scheduler SCHEDULER2 = Schedulers.newSingle("B"); 

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
        Flux<Integer> f1 = Flux.<Integer>create(sink -> {
            int  i =0 ;
            while(!sink.isCancelled()) {
                sink.next(i++);
            }
        })
                .subscribeOn(SCHEDULER1);

        Flux<Integer> f2 = Flux.<Integer>create(sink -> {
            int  i =0 ;
            while(!sink.isCancelled()) {
                sink.next(i--);
            }
        })
                .subscribeOn(SCHEDULER2);

        Flux.merge(f1,f2)
                .take(1000)
                .blockLast();
    }
}
