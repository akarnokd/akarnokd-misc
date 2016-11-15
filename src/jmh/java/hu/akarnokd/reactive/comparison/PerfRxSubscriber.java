package hu.akarnokd.reactive.comparison;

import org.openjdk.jmh.infra.Blackhole;

public final class PerfRxSubscriber extends rx.Subscriber<Object> {

    final Blackhole bh;

    public PerfRxSubscriber(Blackhole bh) {
        this.bh = bh;
    }

    @Override
    public void onStart() {
        bh.consume(true);
    }

    @Override
    public void onCompleted() {
        bh.consume(false);
    }

    @Override
    public void onError(Throwable e) {
        bh.consume(e);
    }

    @Override
    public void onNext(Object t) {
        bh.consume(t);
    }

}
