package hu.akarnokd.rxjava;

import io.reactivex.exceptions.Exceptions;
import rx.*;
import rx.functions.Func1;

public final class CompletableFlatMapSingleToCompletable<T> implements Completable.OnSubscribe {

    final Single<T> source;

    final Func1<? super T, Completable> mapper;

    public CompletableFlatMapSingleToCompletable(Single<T> source, Func1<? super T, Completable> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void call(rx.CompletableSubscriber t) {
        SourceSubscriber<T> parent = new SourceSubscriber<>(t, mapper);
        t.onSubscribe(parent);
        source.subscribe(parent);
    }

    static final class SourceSubscriber<T> extends SingleSubscriber<T> implements rx.CompletableSubscriber {
        final rx.CompletableSubscriber actual;

        final Func1<? super T, Completable> mapper;

        SourceSubscriber(rx.CompletableSubscriber actual, Func1<? super T, Completable> mapper) {
            this.actual = actual;
            this.mapper = mapper;
        }

        @Override
        public void onSuccess(T value) {
            Completable c;

            try {
                c = mapper.call(value);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                onError(ex);
                return;
            }

            if (c == null) {
                onError(new NullPointerException("The mapper returned a null Completable"));
                return;
            }

            c.subscribe(this);
        }

        @Override
        public void onError(Throwable error) {
            actual.onError(error);
        }

        @Override
        public void onCompleted() {
            actual.onCompleted();
        }

        @Override
        public void onSubscribe(Subscription d) {
            add(d);
        }
    }

}
