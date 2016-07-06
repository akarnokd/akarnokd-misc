package hu.akarnokd.rxjava2;

import io.reactivex.*;
import io.reactivex.internal.operators.observable.ObservableSource;

public class SumIntObservable extends ObservableSource<Integer, Integer> {

    public SumIntObservable(Observable<Integer> source) {
        super(source);
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        source.subscribe(new SumIntObserver(observer));
    }
    
    static final class SumIntObserver extends IntReducer {

        public SumIntObserver(Observer<? super Integer> actual) {
            super(actual);
        }

        @Override
        public void onNext(Integer value) {
            if (!hasValue) {
                hasValue = true;
            }
            accumulator += value.intValue();
        }
        
    }
}
