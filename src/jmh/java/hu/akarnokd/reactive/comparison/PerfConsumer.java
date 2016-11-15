package hu.akarnokd.reactive.comparison;

import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.*;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;

public final class PerfConsumer implements Subscriber<Object>, Observer<Object>, SingleObserver<Object>,
CompletableObserver, MaybeObserver<Object>, rx.CompletableSubscriber {

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
    public void onCompleted() {
        bh.consume(false);
    }
    @Override
    public void onSubscribe(rx.Subscription d) {
        bh.consume(d);
    }

}
