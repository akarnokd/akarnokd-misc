package hu.akarnokd.rxjava2;

import io.reactivex.*;
import io.reactivex.internal.operators.observable.ObservableSource;

public class SumLongObservable extends ObservableSource<Long, Long> {

    public SumLongObservable(Observable<Long> source) {
        super(source);
    }

    @Override
    protected void subscribeActual(Observer<? super Long> observer) {
        source.subscribe(new SumLongObserver(observer));
    }
    
    static final class SumLongObserver extends LongReducer {

        public SumLongObserver(Observer<? super Long> actual) {
            super(actual);
        }

        @Override
        public void onNext(Long value) {
            if (!hasValue) {
                hasValue = true;
            }
            accumulator += value.longValue();
        }
        
    }
}
