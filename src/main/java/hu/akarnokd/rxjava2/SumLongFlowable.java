package hu.akarnokd.rxjava2;

import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.internal.operators.flowable.FlowableSource;

public class SumLongFlowable extends FlowableSource<Long, Long> {

    public SumLongFlowable(Flowable<Long> source) {
        super(source);
    }

    @Override
    protected void subscribeActual(Subscriber<? super Long> observer) {
        source.subscribe(new SumLongSubscriber(observer));
    }
    
    static final class SumLongSubscriber extends LongDeferredReducer {

        /** */
        private static final long serialVersionUID = 600979972678601618L;

        public SumLongSubscriber(Subscriber<? super Long> actual) {
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
