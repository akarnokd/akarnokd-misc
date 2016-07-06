package hu.akarnokd.rxjava2;

import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.internal.operators.flowable.FlowableSource;

public class SumIntFlowable extends FlowableSource<Integer, Integer> {

    public SumIntFlowable(Flowable<Integer> source) {
        super(source);
    }

    @Override
    protected void subscribeActual(Subscriber<? super Integer> observer) {
        source.subscribe(new SumIntSubscriber(observer));
    }
    
    static final class SumIntSubscriber extends IntDeferredReducer {

        /** */
        private static final long serialVersionUID = 600979972678601618L;

        public SumIntSubscriber(Subscriber<? super Integer> actual) {
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
