package hu.akarnokd.rxjava;

import java.util.Iterator;
import java.util.concurrent.*;

import hu.akarnokd.rxjava2.internal.subscribers.LambdaSubscriber;
import reactivestreams.commons.publisher.Px;

public class CombineLatestTest {
    static class Iter implements Iterator<Long> {
        long count = 0;
        @Override public boolean hasNext() { return true; }
        @Override public Long next() { return count++; }
    };
    
    public static void main(String[] args) throws Exception {
        ExecutorService exec1 = Executors.newSingleThreadExecutor();
        ExecutorService exec2 = Executors.newSingleThreadExecutor();
        
        try {
            Px<Long> source1 = Px.fromIterable(Iter::new).subscribeOn(exec1);
            Px<Long> source2 = Px.fromIterable(Iter::new).subscribeOn(exec2);
            
            Px.combineLatest(source1, source2, (a, b) -> a + ", " + b)
            .subscribe(new LambdaSubscriber<>(System.out::println, e -> { }, () -> { }, s -> s.request(Long.MAX_VALUE)));
            
            Thread.sleep(2000);
        } finally {
            exec1.shutdown();
            exec2.shutdown();
        }
    }
}
