package hu.akarnokd.rxjava2;

import org.reactivestreams.Publisher;

import io.reactivex.*;

public class Transformers<T> implements FlowableTransformer<T, T>, ObservableTransformer<T, T> {

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Publisher<T> apply(Flowable<T> upstream) {
        // TODO Auto-generated method stub
        return null;
    }

}
