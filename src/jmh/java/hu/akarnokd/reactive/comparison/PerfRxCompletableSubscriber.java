package hu.akarnokd.reactive.comparison;

import org.openjdk.jmh.infra.Blackhole;

import rx.Subscription;

public final class PerfRxCompletableSubscriber implements rx.CompletableSubscriber {

    final Blackhole bh;
    
    public PerfRxCompletableSubscriber(Blackhole bh) {
        this.bh = bh;
    }
    
    @Override
    public void onError(Throwable e) {
        bh.consume(e);
    }

    @Override
    public void onCompleted() {
        bh.consume(true);
    }
    
    @Override
    public void onSubscribe(Subscription d) {
        bh.consume(d);
    }
    
}
