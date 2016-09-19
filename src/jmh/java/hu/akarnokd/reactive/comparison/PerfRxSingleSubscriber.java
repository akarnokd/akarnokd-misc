package hu.akarnokd.reactive.comparison;

import org.openjdk.jmh.infra.Blackhole;

public final class PerfRxSingleSubscriber extends rx.SingleSubscriber<Object> {

    final Blackhole bh;
    
    public PerfRxSingleSubscriber(Blackhole bh) {
        this.bh = bh;
    }
    
    @Override
    public void onError(Throwable e) {
        bh.consume(e);
    }

    @Override
    public void onSuccess(Object t) {
        bh.consume(t);
    }
    
}
