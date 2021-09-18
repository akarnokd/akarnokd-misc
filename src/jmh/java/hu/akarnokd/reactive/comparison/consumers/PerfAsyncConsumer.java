package hu.akarnokd.reactive.comparison.consumers;

import java.util.concurrent.*;

import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Subscription;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import reactor.core.CoreSubscriber;

public final class PerfAsyncConsumer
extends CountDownLatch
implements
FlowableSubscriber<Object>,
CoreSubscriber<Object>,
Observer<Object>,
SingleObserver<Object>,
CompletableObserver,
MaybeObserver<Object>,
rx.CompletableSubscriber {

    final Blackhole bh;

    public PerfAsyncConsumer(Blackhole bh) {
        super(1);
        this.bh = bh;
    }

    @Override
    public void onSubscribe(Subscription s) {
        bh.consume(s);
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onComplete() {
        bh.consume(false);
        countDown();
    }

    @Override
    public void onError(Throwable e) {
        bh.consume(e);
        countDown();
    }

    @Override
    public void onNext(Object t) {
        bh.consume(t);
    }

    @Override
    public void onSuccess(Object value) {
        bh.consume(value);
        countDown();
    }

    @Override
    public void onSubscribe(Disposable d) {
        bh.consume(d);
    }

    @Override
    public void onCompleted() {
        bh.consume(false);
        countDown();
    }
    @Override
    public void onSubscribe(rx.Subscription d) {
        bh.consume(d);
    }

    public void await(int count) {
        await(count, 10);
    }


    public void await(int count, int timeoutSeconds) {
        if (count <= 1000) {
            while (getCount() != 0) { }
        } else {
            try {
                if (!await(timeoutSeconds, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Timeout!");
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
