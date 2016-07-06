package hu.akarnokd.rxjava2;

import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.internal.operators.flowable.FlowableSource;

public class MaxIntFlowable extends FlowableSource<Integer, Integer> {

    public MaxIntFlowable(Flowable<Integer> source) {
        super(source);
    }

    @Override
    protected void subscribeActual(Subscriber<? super Integer> observer) {
        source.subscribe(new MaxIntSubscriber(observer));
    }
    
    static final class MaxIntSubscriber extends IntDeferredReducer {

        /** */
        private static final long serialVersionUID = 600979972678601618L;

        public MaxIntSubscriber(Subscriber<? super Integer> actual) {
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
