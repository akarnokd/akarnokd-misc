package hu.akarnokd.reactive.observables;

import java.util.*;
import java.util.concurrent.Callable;

import io.reactivex.functions.*;
import io.reactivex.internal.util.ExceptionHelper;

/**
 * Synchronous-only, non-backpressured reactive type.
 * @param <T> the element type
 */
public interface IObservable<T> {

    void subscribe(IObserver<? super T> observer);
    
    static <T> IObservable<T> just(T element) {
        return o -> {
            BooleanDisposable d = new BooleanDisposable();
            o.onSubscribe(d);
            if (!d.isDisposed()) {
                o.onNext(element);
                if (!d.isDisposed()) {
                    o.onComplete();
                }
            }
        };
    }

    static IObservable<Integer> characters(CharSequence cs) {
        return o -> {
            BooleanDisposable d = new BooleanDisposable();
            o.onSubscribe(d);
            for (int i = 0; i < cs.length(); i++) {
                if (d.isDisposed()) {
                    return;
                }
                o.onNext((int)cs.charAt(i));
            }
            if (!d.isDisposed()) {
                o.onComplete();
            }
        };
    }

    @SafeVarargs
    static <T> IObservable<T> concatArray(IObservable<T>... sources) {
        return o -> {
            class ConcatObserver implements IObserver<T>, IDisposable {

                IDisposable d;
                
                boolean disposed;
                
                int wip;
                
                int index;
                
                @Override
                public void dispose() {
                    disposed = true;
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    if (disposed) {
                        d.dispose();
                    } else {
                        this.d = d;
                    }
                }

                @Override
                public void onNext(T element) {
                    o.onNext(element);
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    if (wip++ == 0) {
                        do {
                            if (disposed) {
                                return;
                            }
                            
                            if (index == sources.length) {
                                o.onComplete();
                                return;
                            }
                            
                            sources[index++].subscribe(this);
                        } while (--wip != 0);
                    }
                }
                
            }
            
            ConcatObserver obs = new ConcatObserver();
            
            o.onSubscribe(obs);
            obs.onComplete();
        };
    }

    static <T> IObservable<T> fromIterable(Iterable<? extends T> source) {
        return o -> {
            BooleanDisposable d = new BooleanDisposable();
            o.onSubscribe(d);
            for (T item : source) {
                if (d.isDisposed()) {
                    return;
                }
                o.onNext(item);
            }
            if (!d.isDisposed()) {
                o.onComplete();
            }
        };
    }
    
    default <C> IObservable<C> collect(Callable<C> collectionSupplier, BiConsumer<? super C, ? super T> collector) {
        return o -> {
            C collection;
            
            try {
                collection = collectionSupplier.call();
            } catch (Throwable ex) {
                BooleanDisposable d = new BooleanDisposable();
                o.onSubscribe(d);
                o.onError(ex);
                return;
            }
            
            class Collector implements IObserver<T>, IDisposable {

                IDisposable d;

                boolean disposed;

                @Override
                public void dispose() {
                    disposed = true;
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                }

                @Override
                public void onNext(T element) {
                    try {
                        collector.accept(collection, element);
                    } catch (Throwable ex) {
                        dispose();
                        o.onError(ex);
                    }
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    o.onNext(collection);
                    if (!disposed) {
                        o.onComplete();
                    }
                }
                
            }
            
            subscribe(new Collector());
        };
    }
    
    default <R> IObservable<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return o -> {
            class FlatMapper implements IObserver<T>, IDisposable {
                IDisposable d;
                
                boolean disposed;

                @Override
                public void dispose() {
                    disposed = true;
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                }

                @Override
                public void onNext(T element) {
                    Iterator<? extends R> it;
                    
                    try {
                        it = mapper.apply(element).iterator();
                    } catch (Throwable ex) {
                        dispose();
                        o.onError(ex);
                        return;
                    }
                    
                    while (it.hasNext()) {
                        if (disposed) {
                            break;
                        }
                        o.onNext(it.next());
                    }
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    o.onComplete();
                }
            }
            subscribe(new FlatMapper());
        };
    }

    default <R> IObservable<R> map(Function<? super T, ? extends R> mapper) {
        return o -> {
            class Mapper implements IObserver<T>, IDisposable {
                IDisposable d;
                
                @Override
                public void dispose() {
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                }

                @Override
                public void onNext(T element) {
                    R v;
                    
                    try {
                        v = mapper.apply(element);
                    } catch (Throwable ex) {
                        dispose();
                        o.onError(ex);
                        return;
                    }
                    
                    o.onNext(v);
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    o.onComplete();
                }
            }
            subscribe(new Mapper());
        };
    }

    default IObservable<T> filter(Predicate<? super T> predicate) {
        return o -> {
            class Filterer implements IObserver<T>, IDisposable {
                IDisposable d;
                
                @Override
                public void dispose() {
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                }

                @Override
                public void onNext(T element) {
                    boolean v;
                    
                    try {
                        v = predicate.test(element);
                    } catch (Throwable ex) {
                        dispose();
                        o.onError(ex);
                        return;
                    }
                    
                    if (v) {
                        o.onNext(element);
                    }
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    o.onComplete();
                }
            }
            subscribe(new Filterer());
        };
    }

    @SuppressWarnings("unchecked")
    default IObservable<Integer> sumInt() {
        return o -> {
            class SumInt implements IObserver<Number>, IDisposable {
                IDisposable d;
                
                int sum;
                boolean hasValue;
                
                @Override
                public void dispose() {
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                }

                @Override
                public void onNext(Number element) {
                    if (!hasValue) {
                        hasValue = true;
                    }
                    sum += element.intValue();
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    if (hasValue) {
                        o.onNext(sum);
                    }
                    o.onComplete();
                }
            }
            ((IObservable<Number>)this).subscribe(new SumInt());
        };
    }

    @SuppressWarnings("unchecked")
    default IObservable<Long> sumLong() {
        return o -> {
            class SumLong implements IObserver<Number>, IDisposable {
                IDisposable d;
                
                long sum;
                boolean hasValue;
                
                @Override
                public void dispose() {
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                }

                @Override
                public void onNext(Number element) {
                    if (!hasValue) {
                        hasValue = true;
                    }
                    sum += element.longValue();
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    if (hasValue) {
                        o.onNext(sum);
                    }
                    o.onComplete();
                }
            }
            ((IObservable<Number>)this).subscribe(new SumLong());
        };
    }

    @SuppressWarnings("unchecked")
    default IObservable<Integer> maxInt() {
        return o -> {
            class MaxInt implements IObserver<Number>, IDisposable {
                IDisposable d;
                
                int max;
                boolean hasValue;
                
                @Override
                public void dispose() {
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                }

                @Override
                public void onNext(Number element) {
                    if (!hasValue) {
                        hasValue = true;
                        max = element.intValue();
                    } else {
                        max = Math.max(max, element.intValue());
                    }
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    if (hasValue) {
                        o.onNext(max);
                    }
                    o.onComplete();
                }
            }
            ((IObservable<Number>)this).subscribe(new MaxInt());
        };
    }

    default IObservable<T> take(long n) {
        return o -> {
            class Take implements IObserver<T>, IDisposable {
                IDisposable d;
                
                long remaining;
                
                Take(long n) {
                    this.remaining = n;
                }
                
                @Override
                public void dispose() {
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                    if (remaining == 0) {
                        d.dispose();
                    }
                }

                @Override
                public void onNext(T element) {
                    long r = remaining;
                    if (r > 0) {
                        remaining = --r;
                        o.onNext(element);
                        if (r == 0) {
                            dispose();
                            o.onComplete();
                        }
                    }
                }

                @Override
                public void onError(Throwable cause) {
                    if (remaining > 0) {
                        o.onError(cause);
                    }
                }

                @Override
                public void onComplete() {
                    if (remaining > 0) {
                        o.onComplete();
                    }
                }
            }
            subscribe(new Take(n));
        };
    }

    default IObservable<T> skip(long n) {
        return o -> {
            class Skip implements IObserver<T>, IDisposable {
                IDisposable d;
                
                long remaining;
                
                Skip(long n) {
                    this.remaining = n;
                }

                @Override
                public void dispose() {
                    d.dispose();
                }

                @Override
                public void onSubscribe(IDisposable d) {
                    this.d = d;
                    o.onSubscribe(this);
                }

                @Override
                public void onNext(T element) {
                    long r = remaining;
                    if (r == 0) {
                        o.onNext(element);
                    } else {
                        remaining = r - 1;
                    }
                }

                @Override
                public void onError(Throwable cause) {
                    o.onError(cause);
                }

                @Override
                public void onComplete() {
                    o.onComplete();
                }
            }
            subscribe(new Skip(n));
        };
    }

    default T first() {
        class First implements IObserver<T> {
            T item;

            Throwable error;

            IDisposable d;
            
            @Override
            public void onSubscribe(IDisposable d) {
                this.d = d;
            }

            @Override
            public void onNext(T element) {
                item = element;
                d.dispose();
            }

            @Override
            public void onError(Throwable cause) {
                error = cause;
            }

            @Override
            public void onComplete() {
            }
        }
        
        First f = new First();
        subscribe(f);
        if (f.error != null) {
            throw ExceptionHelper.wrapOrThrow(f.error);
        }
        if (f.item == null) {
            throw new NoSuchElementException();
        }
        return f.item;
    }

    default T last() {
        class Last implements IObserver<T> {
            T item;

            Throwable error;

            @Override
            public void onSubscribe(IDisposable d) {
            }

            @Override
            public void onNext(T element) {
                item = element;
            }

            @Override
            public void onError(Throwable cause) {
                error = cause;
            }

            @Override
            public void onComplete() {
            }
        }
        
        Last f = new Last();
        subscribe(f);
        if (f.error != null) {
            throw ExceptionHelper.wrapOrThrow(f.error);
        }
        if (f.item == null) {
            throw new NoSuchElementException();
        }
        return f.item;
    }
}
