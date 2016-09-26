package hu.akarnokd.reactive.comparison;

import java.util.concurrent.*;

import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.*;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;

public final class PerfAsyncConsumer implements Subscriber<Object>, Observer<Object>, SingleObserver<Object>,
CompletableObserver, MaybeObserver<Object>, rx.CompletableSubscriber {

    final CountDownLatch cdl;
    
    final Blackhole bh;
    
    public PerfAsyncConsumer(Blackhole bh) {
        this.bh = bh;
        this.cdl = new CountDownLatch(1);
    }
    
    @Override
    public void onSubscribe(Subscription s) {
        bh.consume(s);
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onComplete() {
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

    @Override
    public void onSuccess(Object value) {
        bh.consume(value);
        cdl.countDown();
    }

    @Override
    public void onSubscribe(Disposable d) {
        bh.consume(d);
    }
    
    @Override
    public void onCompleted() {
        bh.consume(false);
        cdl.countDown();
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
            while (cdl.getCount() != 0) { }
        } else {
            try {
                if (!cdl.await(timeoutSeconds, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Timeout!");
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
