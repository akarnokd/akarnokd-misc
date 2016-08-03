package hu.akarnokd.rxjava2;

import org.reactivestreams.*;

import reactor.core.Fuseable;
import reactor.core.publisher.MonoSource;

public class SumLongMono extends MonoSource<Long, Long> implements Fuseable {

    public SumLongMono(Publisher<Long> source) {
        super(source);
    }

    @Override
    public void subscribe(Subscriber<? super Long> observer) {
        source.subscribe(new SumLongSubscriber(observer));
    }
    
    static final class SumLongSubscriber extends LongDeferredReducerCore {

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
