package hu.akarnokd.rxjava2;

import java.util.*;
import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.*;

public class StageDelays {

    @Test
    public void test() {
        TestScheduler ts = new TestScheduler();
        
        Map<Long, Long[]> state = new LinkedHashMap<>();
        
        Flowable.interval(100, TimeUnit.MILLISECONDS, ts)
        .onBackpressureLatest()
        .doOnNext(v -> state.put(v, new Long[4]))
        .observeOn(ts, false, 1)
        .doOnNext(v -> { 
            System.out.println("p50: " + v + " @ " + ts.now(TimeUnit.MILLISECONDS));
            state.get(v)[0] = ts.now(TimeUnit.MILLISECONDS);
        })
        .delay(50, TimeUnit.MILLISECONDS, ts)
        .observeOn(ts, false, 1)
        .doOnNext(v -> { 
            System.out.println("p500: " + v + " @ " + ts.now(TimeUnit.MILLISECONDS));
            state.get(v)[1] = ts.now(TimeUnit.MILLISECONDS);
        })
        .delay(500, TimeUnit.MILLISECONDS, ts)
        .observeOn(ts, false, 1)
        .doOnNext(v -> { 
            System.out.println("p200: " + v + " @ " + ts.now(TimeUnit.MILLISECONDS));
            state.get(v)[2] = ts.now(TimeUnit.MILLISECONDS);
        })
        .delay(200, TimeUnit.MILLISECONDS, ts)
        .subscribe(v -> {
            System.out.println("END: " + v + " @ " + ts.now(TimeUnit.MILLISECONDS));
            state.get(v)[3] = ts.now(TimeUnit.MILLISECONDS);
        });
        
        ts.advanceTimeBy(2000, TimeUnit.MILLISECONDS);
        
        for (Map.Entry<Long, Long[]> e : state.entrySet()) {
            System.out.println(e.getKey());
            System.out.println("   p50: " + e.getValue()[0]);
            System.out.println("  p500: " + e.getValue()[1]);
            System.out.println("  p200: " + e.getValue()[2]);
            System.out.println("   END: " + e.getValue()[3]);
        }
    }
    
    @Test
    public void test2() throws Exception {
Scheduler ts = Schedulers.newThread();

long start = ts.now(TimeUnit.MILLISECONDS);

Map<Long, Long[]> state = new ConcurrentHashMap<>();

Disposable d = Flowable.interval(100, TimeUnit.MILLISECONDS, ts)
.onBackpressureLatest()
.doOnNext(v -> state.put(v, new Long[] { 0L, 0L, 0L, 0L }))
.delay(0, TimeUnit.MILLISECONDS, ts)
.doOnNext(v -> { 
    System.out.println("p50: " + v + " @ " + (ts.now(TimeUnit.MILLISECONDS) - start));
    state.get(v)[0] = ts.now(TimeUnit.MILLISECONDS) - start;
    Thread.sleep(50);
})
.delay(0, TimeUnit.MILLISECONDS, ts)
.doOnNext(v -> { 
    System.out.println("p500: " + v + " @ " + (ts.now(TimeUnit.MILLISECONDS) - start));
    state.get(v)[1] = ts.now(TimeUnit.MILLISECONDS) - start;
    Thread.sleep(500);
})
.delay(0, TimeUnit.MILLISECONDS, ts)
.doOnNext(v -> { 
    System.out.println("p200: " + v + " @ " + (ts.now(TimeUnit.MILLISECONDS) - start));
    state.get(v)[2] = ts.now(TimeUnit.MILLISECONDS) - start;
    Thread.sleep(200);
})
.rebatchRequests(1)
.subscribe(v -> {
    System.out.println("END: " + v + " @ " + (ts.now(TimeUnit.MILLISECONDS)  - start));
    state.get(v)[3] = ts.now(TimeUnit.MILLISECONDS) - start;
}, e -> { });

Thread.sleep(5000);

d.dispose();

Thread.sleep(200);

for (long v = 0; v < 100; v++) {
    if (state.containsKey(v)) {
        System.out.println(v);
        Long[] array = state.get(v);
        
        System.out.println("   p50: " + array[0]);
        System.out.println("  p500: " + array[1]);
        System.out.println("      : " + (array[1] - array[0]));
        System.out.println("  p200: " + array[2]);
        System.out.println("      : " + (array[2] - array[1]));
        System.out.println("   END: " + array[3]);
        System.out.println("      : " + (array[3] - array[2]));
    }
}
    }
}
