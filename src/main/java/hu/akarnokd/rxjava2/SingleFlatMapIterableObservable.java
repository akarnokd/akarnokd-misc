package hu.akarnokd.rxjava2;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.DisposableHelper;

public final class SingleFlatMapIterableObservable<T, R> extends Observable<R> {
    
    final SingleSource<T> source;
    
    final Function<? super T, ? extends Iterable<? extends R>> mapper;

    public SingleFlatMapIterableObservable(SingleSource<T> source,
            Function<? super T, ? extends Iterable<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }
    
    @Override
    protected void subscribeActual(Observer<? super R> s) {
        source.subscribe(new FlatMapIterableObserver<T, R>(s, mapper));
    }

    static final class FlatMapIterableObserver<T, R> 
    extends AtomicInteger
    implements SingleObserver<T>, Disposable {
        
        private static final long serialVersionUID = -8938804753851907758L;

        final Observer<? super R> actual;
        
        final Function<? super T, ? extends Iterable<? extends R>> mapper;
        
        Disposable d;
        
        volatile Iterator<? extends R> it;
        
        volatile boolean cancelled;

        public FlatMapIterableObserver(Observer<? super R> actual,
                Function<? super T, ? extends Iterable<? extends R>> mapper) {
            this.actual = actual;
            this.mapper = mapper;
        }
        
        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.d, d)) {
                this.d = d;
                
                actual.onSubscribe(this);
            }
        }
        
        @Override
        public void onSuccess(T value) {
            Iterator<? extends R> iter;
            boolean has;
            try {
                
                iter = mapper.apply(value).iterator();

                has = iter.hasNext();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                actual.onError(ex);
                return;
            }
            
            if (!has) {
                actual.onComplete();
                return;
            }
            this.it = iter;
            drain();
        }
        
        @Override
        public void onError(Throwable e) {
            d = DisposableHelper.DISPOSED;
            actual.onError(e);
        }
        
        @Override
        public void dispose() {
            cancelled = true;
            d.dispose();
            d = DisposableHelper.DISPOSED;
        }
        
        @Override
        public boolean isDisposed() {
            return cancelled;
        }
        
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }
            
            int missed = 1;
            
            Iterator<? extends R> iter = this.it;
            
            Observer<? super R> a = actual;
            
            for (;;) {
                
                if (iter != null) {
                    for (;;) {
                        if (cancelled) {
                            return;
                        }
                        
                        R v;
                        
                        try {
                            v = iter.next();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            a.onError(ex);
                            return;
                        }
                        
                        a.onNext(v);
                        
                        if (cancelled) {
                            return;
                        }
                        
                        
                        boolean b;
                        
                        try {
                            b = iter.hasNext();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            a.onError(ex);
                            return;
                        }
                        
                        if (!b) {
                            a.onComplete();
                            return;
                        }
                    }
                }
                
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
                
                if (iter == null) {
                    iter = it;
                }
            }
        }
    }
}
