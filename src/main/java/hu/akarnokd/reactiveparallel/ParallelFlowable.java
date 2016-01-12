package hu.akarnokd.reactiveparallel;

import java.util.function.*;

import org.reactivestreams.Publisher;

import hu.akarnokd.rxjava2.Observable;

public class ParallelFlowable<T> {
    
    protected ParallelFlowable() {
    }
    
    public final <R> ParallelFlowable<R> map(Function<? super T, ? extends R> mapper) {
        throw new UnsupportedOperationException();
    }
    
    public final ParallelFlowable<T> filter(Predicate<? super T> predicate) {
        throw new UnsupportedOperationException();
    }
    
    public final <R> ParallelFlowable<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        throw new UnsupportedOperationException();
    }
    
    public final Observable<T> toObservable() {
        throw new UnsupportedOperationException();
    }
}
