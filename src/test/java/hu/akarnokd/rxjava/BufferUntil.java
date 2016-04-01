package hu.akarnokd.rxjava;

import java.util.*;

import rx.Observable.Operator;
import rx.Subscriber;
import rx.functions.Func1;

    public final class BufferUntil<T> 
    implements Operator<List<T>, T>{
        
        final Func1<T, Boolean> boundaryPredicate;
    
        public BufferUntil(Func1<T, Boolean> boundaryPredicate) {
            this.boundaryPredicate = boundaryPredicate;
        }
        
        @Override
        public Subscriber<? super T> call(
                Subscriber<? super List<T>> child) {
            BufferWhileSubscriber parent = 
                    new BufferWhileSubscriber(child);
            child.add(parent);
            return parent;
        }
        
        final class BufferWhileSubscriber extends Subscriber<T> {
            final Subscriber<? super List<T>> actual;
            
            List<T> buffer = new ArrayList<>();
            
            /**
             * @param actual
             */
            public BufferWhileSubscriber(
                    Subscriber<? super List<T>> actual) {
                this.actual = actual;
            }
    
            @Override
            public void onNext(T t) {
                buffer.add(t);
                if (boundaryPredicate.call(t)) {
                    actual.onNext(buffer);
                    buffer = new ArrayList<>();
                }
            }
            
            @Override
            public void onError(Throwable e) {
                buffer = null;
                actual.onError(e);
            }
            
            @Override
            public void onCompleted() {
                List<T> b = buffer;
                buffer = null;
                if (!b.isEmpty()) {
                    actual.onNext(b);
                }
                actual.onCompleted();
            }
        }
    }
