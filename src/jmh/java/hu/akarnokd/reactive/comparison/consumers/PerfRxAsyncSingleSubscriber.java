package hu.akarnokd.reactive.comparison.consumers;

import java.util.concurrent.*;

import org.openjdk.jmh.infra.Blackhole;

public final class PerfRxAsyncSingleSubscriber extends rx.SingleSubscriber<Object> {

    final CountDownLatch cdl;

    final Blackhole bh;

    public PerfRxAsyncSingleSubscriber(Blackhole bh) {
        this.bh = bh;
        this.cdl = new CountDownLatch(1);
    }

    @Override
    public void onError(Throwable e) {
        bh.consume(e);
        cdl.countDown();
    }

    @Override
    public void onSuccess(Object t) {
        bh.consume(t);
        cdl.countDown();
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
