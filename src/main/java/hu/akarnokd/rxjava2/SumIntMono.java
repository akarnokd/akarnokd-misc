package hu.akarnokd.rxjava2;

import org.reactivestreams.*;

import reactor.core.Fuseable;
import reactor.core.publisher.MonoSource;

public class SumIntMono extends MonoSource<Integer, Integer> implements Fuseable {

    public SumIntMono(Publisher<Integer> source) {
        super(source);
    }

    @Override
    public void subscribe(Subscriber<? super Integer> observer) {
        source.subscribe(new SumIntSubscriber(observer));
    }
    
    static final class SumIntSubscriber extends IntDeferredReducerCore {

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
