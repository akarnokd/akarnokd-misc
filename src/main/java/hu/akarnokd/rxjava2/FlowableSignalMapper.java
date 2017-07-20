package hu.akarnokd.rxjava2;

import java.util.concurrent.Callable;

import org.reactivestreams.*;

import io.reactivex.*;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.plugins.RxJavaPlugins;

public final class FlowableSignalMapper<R> extends Flowable<R> {

    final Flowable<Object> source;
    
    final Callable<R> mapper;

    public FlowableSignalMapper(Flowable<Object> source, Callable<R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new SignalMapperSubscriber<R>(s, mapper));
    }

    static final class SignalMapperSubscriber<R> implements FlowableSubscriber<Object>, Subscription {

        final Subscriber<? super R> actual;

        final Callable<R> mapper;

        Subscription upstream;

        boolean done;

        SignalMapperSubscriber(Subscriber<? super R> actual, Callable<R> mapper) {
            this.actual = actual;
            this.mapper = mapper;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(Object t) {
            if (done) {
                return;
            }
            R v;
            try {
                v = mapper.call();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.cancel();
                onError(ex);
                return;
            }
            if (v != null) {
                actual.onNext(v);
            } else {
                upstream.request(1);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            actual.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            actual.onComplete();
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }
    }
}
