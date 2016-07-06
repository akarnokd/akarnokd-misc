package hu.akarnokd.rxjava2;

import org.reactivestreams.*;

import reactor.core.flow.Fuseable;
import reactor.core.publisher.MonoSource;

public class MaxIntMono extends MonoSource<Integer, Integer> implements Fuseable {

    public MaxIntMono(Publisher<Integer> source) {
        super(source);
    }

    @Override
    public void subscribe(Subscriber<? super Integer> observer) {
        source.subscribe(new MaxIntSubscriber(observer));
    }
    
    static final class MaxIntSubscriber extends IntDeferredReducerCore {

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
