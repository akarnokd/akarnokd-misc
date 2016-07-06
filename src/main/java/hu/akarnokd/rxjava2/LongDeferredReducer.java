package hu.akarnokd.rxjava2;

import org.reactivestreams.*;

import io.reactivex.internal.subscriptions.DeferredScalarSubscription;

public abstract class LongDeferredReducer extends DeferredScalarSubscription<Long> 
implements Subscriber<Long> {

    /** */
    private static final long serialVersionUID = -5440866300413185735L;

    Subscription s;
    
    long accumulator;
    
    boolean hasValue;
    
    public LongDeferredReducer(Subscriber<? super Long> actual) {
        super(actual);
    }

    @Override
    public final void onSubscribe(Subscription s) {
        this.s = s;
        
        actual.onSubscribe(this);
        
        s.request(Long.MAX_VALUE);
    }
    
    @Override
    public final void onError(Throwable t) {
        actual.onError(t);
    }
    
    @Override
    public final void onComplete() {
        if (hasValue) {
            complete(accumulator);
        } else {
            actual.onComplete();
        }
    }
}
