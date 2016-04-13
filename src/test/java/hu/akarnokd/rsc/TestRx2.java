package hu.akarnokd.rsc;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.reactivestreams.*;

import rsc.publisher.*;

public class TestRx2 {

    @Test
    public void test() throws InterruptedException {
        Iterable<Long> iterable = new Iterable<Long>() {
            final AtomicLong count = new AtomicLong(0);
            @Override
            public Iterator<Long> iterator() {
                return new Iterator<Long>() {
                    { System.out.println("startup"); }
                    @Override
                    public boolean hasNext() { return true; }

                    @Override
                    public Long next() {
//                        System.out.println("nxt");
                        try { Thread.sleep(200); } catch (InterruptedException e) {}
                        final long l = count.incrementAndGet();
//                        System.out.println("inc: "+l);
                        return l;
                    }
                };
            }
        };


        final ConnectablePublisher<Long> flux = Px.fromIterable(iterable).publish();
//        final Observable<Long> flux = Observable.fromIterable(iterable);

        final Px<Long> intervalOdd =
                flux.filter(p -> {
//                    System.out.println("odd "+p);
                    return p % 2 != 0;
                });

        final Px<Long> intervalEven =
                flux.filter(p -> {
//                    System.out.println("even "+p);
                    return p % 2 == 0;
                });

        Px.combineLatest(intervalOdd, intervalEven, (a,b) -> Arrays.asList(a, b))
                .subscribe(new Subscriber<List<Long>>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }
                    
                    @Override
                    public void onNext(List<Long> tuple) {
                        System.out.printf("(%d,%d)\n", tuple.get(0), tuple.get(1));
                    }
                    
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }
                    
                    @Override
                    public void onComplete() {
                        System.out.println("Done");
                    }
                });


        flux.connect();

        Thread.currentThread().join();
    }
}