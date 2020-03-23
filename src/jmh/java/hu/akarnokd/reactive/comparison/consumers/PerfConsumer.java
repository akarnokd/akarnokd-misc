package hu.akarnokd.reactive.comparison.consumers;

import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Subscription;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import reactor.core.CoreSubscriber;

public final class PerfConsumer implements
FlowableSubscriber<Object>, CoreSubscriber<Object>, Observer<Object>, SingleObserver<Object>,
CompletableObserver, MaybeObserver<Object>, rx.CompletableSubscriber, 
io.reactivex.rxjava3.core.FlowableSubscriber<Object>, 
io.reactivex.rxjava3.core.Observer<Object>, 
io.reactivex.rxjava3.core.SingleObserver<Object>,
io.reactivex.rxjava3.core.CompletableObserver, 
io.reactivex.rxjava3.core.MaybeObserver<Object>,
java.util.concurrent.Flow.Subscriber<Object>
{

    final Blackhole bh;

    public PerfConsumer(Blackhole bh) {
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
    }

    @Override
    public void onError(Throwable e) {
        bh.consume(e);
    }

    @Override
    public void onNext(Object t) {
        bh.consume(t);
    }

    @Override
    public void onSuccess(Object value) {
        bh.consume(value);
    }

    @Override
    public void onSubscribe(Disposable d) {
        bh.consume(d);
    }


    @Override
    public void onSubscribe(io.reactivex.rxjava3.disposables.Disposable d) {
        bh.consume(d);
    }

    @Override
    public void onCompleted() {
        bh.consume(false);
    }

    @Override
    public void onSubscribe(rx.Subscription d) {
        bh.consume(d);
    }

    @Override
    public void onSubscribe(
            java.util.concurrent.Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
        bh.consume(subscription);
    }
}
