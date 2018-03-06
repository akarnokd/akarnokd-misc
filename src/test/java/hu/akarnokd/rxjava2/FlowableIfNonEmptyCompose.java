package hu.akarnokd.rxjava2;

import java.util.function.Function;

import io.reactivex.*;

public final class FlowableIfNonEmptyCompose<T, R> extends Completable {

    final Flowable<T> source;

    final Function<? super Flowable<T>, ? extends Completable> composer;

    public FlowableIfNonEmptyCompose(Flowable<T> source,
            Function<? super Flowable<T>, ? extends Completable> composer) {
        this.source = source;
        this.composer = composer;
    }

    @Override
    protected void subscribeActual(CompletableObserver s) {
        // TODO Auto-generated method stub
        
    }
    
}
