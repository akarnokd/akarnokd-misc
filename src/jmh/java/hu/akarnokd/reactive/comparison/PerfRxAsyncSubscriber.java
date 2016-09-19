package hu.akarnokd.reactive.comparison;

import java.util.concurrent.*;

import org.openjdk.jmh.infra.Blackhole;

public final class PerfRxAsyncSubscriber extends rx.Subscriber<Object> {

    final CountDownLatch cdl;

    final Blackhole bh;
    
    public PerfRxAsyncSubscriber(Blackhole bh) {
        this.bh = bh;
        this.cdl = new CountDownLatch(1);
    }
    
    @Override
    public void onStart() {
        bh.consume(true);
    }

    @Override
    public void onCompleted() {
        bh.consume(false);
        cdl.countDown();
    }

    @Override
    public void onError(Throwable e) {
        bh.consume(e);
        cdl.countDown();
    }

    @Override
    public void onNext(Object t) {
        bh.consume(t);
    }
    
    
    public void await(int count) {
        if (count <= 1000) {
            while (cdl.getCount() != 0) { }
        } else {
            try {
                if (!cdl.await(10, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Timeout!");
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
