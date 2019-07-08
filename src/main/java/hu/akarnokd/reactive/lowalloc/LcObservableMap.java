package hu.akarnokd.reactive.lowalloc;

import java.util.function.Function;

import io.reactivex.disposables.Disposable;

final class LcObservableMap<T, R> extends LcIntermediateObservable<T, R> implements Disposable {

    final Function<T, R> mapper;

    final LcObservable<T> source;

    Disposable upstream;

    LcObservableMap(LcObservable<T> source, Function<T, R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void onSubscribe(Disposable d) {
        upstream = d;
        downstream.onSubscribe(this);
    }

    @Override
    public void onNext(T t) {
        downstream.onNext(mapper.apply(t));
    }

    @Override
    public void onError(Throwable e) {
        downstream.onError(e);
    }

    @Override
    public void onComplete() {
        downstream.onComplete();
    }

    @Override
    protected void subscribeActual() {
        source.subscribe(this);
    }

    @Override
    protected LcObservableMap<T, R> createFresh() {
        return new LcObservableMap<T, R>(source, mapper);
    }

    @Override
    public void dispose() {
        upstream.dispose();
    }

    @Override
    public boolean isDisposed() {
        return upstream.isDisposed();
    }
}
