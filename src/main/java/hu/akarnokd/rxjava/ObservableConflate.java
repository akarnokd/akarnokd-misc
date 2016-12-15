package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import io.reactivex.exceptions.MissingBackpressureException;
import rx.*;
import rx.Observable.*;
import rx.Scheduler.Worker;
import rx.functions.Action0;
import rx.internal.operators.BackpressureUtils;
import rx.observers.SerializedSubscriber;

public final class ObservableConflate<T> implements OnSubscribe<T>, Transformer<T, T> {

    final Observable<T> source;

    final long timeout;

    final TimeUnit unit;

    final Scheduler scheduler;

    public ObservableConflate(long timeout, TimeUnit unit, Scheduler scheduler) {
        this(null, timeout, unit, scheduler);
    }

    public ObservableConflate(Observable<T> source, long timeout, TimeUnit unit, Scheduler scheduler) {
        this.source = source;
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    @Override
    public void call(Subscriber<? super T> t) {
        t = new SerializedSubscriber<>(t);

        Worker worker = scheduler.createWorker();

        ConflateSubscriber<T> parent = new ConflateSubscriber<>(t, timeout, unit, worker);
        t.add(parent);
        t.add(worker);
        t.setProducer(parent.requested);

        source.subscribe(parent);
    }

    @Override
    public Observable<T> call(Observable<T> t) {
        return Observable.create(new ObservableConflate<>(t, timeout, unit, scheduler));
    }

    static final class ConflateSubscriber<T> extends Subscriber<T> implements Action0 {

        static final Object EMPTY = new Object();

        final Subscriber<? super T> actual;

        final long timeout;

        final TimeUnit unit;

        final Worker worker;

        final AtomicReference<Object> current;

        final Requested requested;

        volatile boolean gate;

        public ConflateSubscriber(Subscriber<? super T> actual, long timeout, TimeUnit unit, Worker worker) {
            this.actual = actual;
            this.timeout = timeout;
            this.unit = unit;
            this.worker = worker;
            this.current = new AtomicReference<>(EMPTY);
            this.requested = new Requested();
            this.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(T t) {
            if (!gate) {
                gate = true;

                if (emit(t)) {
                    worker.schedule(this, timeout, unit);
                }
            } else {
                current.lazySet(t);
            }
        }

        @Override
        public void onError(Throwable e) {
            actual.onError(e);
            worker.unsubscribe();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onCompleted() {
            Object o = current.getAndSet(EMPTY);
            if (o != EMPTY) {
                if (!emit((T)o)) {
                    return;
                }
            }
            actual.onCompleted();
            worker.unsubscribe();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void call() {
            Object o = current.getAndSet(EMPTY);
            if (o == EMPTY) {
                gate = false;
            } else {
                if (emit((T)o)) {
                    worker.schedule(this, timeout, unit);
                }
            }
        }
        
        boolean emit(T v) {
            if (requested.get() != 0L) {
                actual.onNext(v);
                requested.producedOne();
                return true;
            }
            unsubscribe();

            actual.onError(new MissingBackpressureException("Could not emit value due to lack of requests"));
            return false;
        }

        final class Requested extends AtomicLong implements Producer {

            private static final long serialVersionUID = 5469053227556974007L;

            @Override
            public void request(long n) {
                if (n > 0L) {
                    BackpressureUtils.getAndAddRequest(this, n);
                }
                else if (n < 0L) {
                    throw new IllegalArgumentException("n >= 0 required but it was " + n);
                }
            }
            
            void producedOne() {
                BackpressureUtils.produced(this, 1L);
            }
        }
    }
}
