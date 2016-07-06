package hu.akarnokd.rxjava2;

import io.reactivex.*;
import io.reactivex.internal.operators.observable.ObservableSource;

public class MaxIntObservable extends ObservableSource<Integer, Integer> {

    public MaxIntObservable(Observable<Integer> source) {
        super(source);
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        source.subscribe(new MaxIntObserver(observer));
    }
    
    static final class MaxIntObserver extends IntReducer {

        public MaxIntObserver(Observer<? super Integer> actual) {
            super(actual);
        }

        @Override
        public void onNext(Integer value) {
            if (!hasValue) {
                hasValue = true;
                accumulator = value.intValue();
            } else {
                accumulator = Math.max(accumulator, value.intValue());
            }
        }
        
    }
}
