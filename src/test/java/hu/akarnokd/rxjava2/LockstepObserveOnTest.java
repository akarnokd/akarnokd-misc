package hu.akarnokd.rxjava2;

    import java.util.concurrent.atomic.*;
    
    import org.junit.Test;
    import org.reactivestreams.*;
    
    import io.reactivex.*;
    import io.reactivex.Scheduler.Worker;
    import io.reactivex.internal.util.BackpressureHelper;
    import io.reactivex.schedulers.Schedulers;
    
    public class LockstepObserveOnTest {
    
        @Test
        public void test() {
            Flowable.generate(() -> 0, (s, e) -> {
                System.out.println("Generating " + s);
                Thread.sleep(500);
                e.onNext(s);
                return s + 1;
            })
            .subscribeOn(Schedulers.io())
            .compose(new LockstepObserveOn<>(Schedulers.computation()))
            .map(v -> {
                Thread.sleep(250);
                System.out.println("Processing " + v);
                Thread.sleep(250);
                return v;
            })
            .take(50)
            .blockingSubscribe();
        }
        
        static final class LockstepObserveOn<T> extends Flowable<T> 
        implements FlowableTransformer<T, T> {
            
            final Flowable<T> source;
            
            final Scheduler scheduler;
    
            LockstepObserveOn(Scheduler scheduler) {
                this(null, scheduler);
            }
    
            LockstepObserveOn(Flowable<T> source, Scheduler scheduler) {
                this.source = source;
                this.scheduler = scheduler;
            }
            
            @Override
            protected void subscribeActual(Subscriber<? super T> subscriber) {
                source.subscribe(new LockstepObserveOnSubscriber<>(subscriber, scheduler.createWorker()));
            }
            
            @Override
            public Publisher<T> apply(Flowable<T> upstream) {
                return new LockstepObserveOn<>(upstream, scheduler);
            }
            
            static final class LockstepObserveOnSubscriber<T>
            implements FlowableSubscriber<T>, Subscription, Runnable {
                
                final Subscriber<? super T> actual;
                
                final Worker worker;
                
                final AtomicReference<T> item;
                
                final AtomicLong requested;
                
                final AtomicInteger wip;
                
                Subscription upstream;
                
                volatile boolean cancelled;
                
                volatile boolean done;
                Throwable error;
                
                long emitted;
                
                LockstepObserveOnSubscriber(Subscriber<? super T> actual, Worker worker) {
                    this.actual = actual;
                    this.worker = worker;
                    this.item = new AtomicReference<>();
                    this.requested = new AtomicLong();
                    this.wip = new AtomicInteger();
                }
                
                @Override
                public void onSubscribe(Subscription s) {
                    upstream = s;
                    actual.onSubscribe(this);
                    s.request(1);
                }
                
                @Override
                public void onNext(T t) {
                    item.lazySet(t);
                    schedule();
                }
                
                @Override
                public void onError(Throwable t) {
                    error = t;
                    done = true;
                    schedule();
                }
                
                @Override
                public void onComplete() {
                    done = true;
                    schedule();
                }
                
                @Override
                public void request(long n) {
                    BackpressureHelper.add(requested, n);
                    schedule();
                }
                
                @Override
                public void cancel() {
                    cancelled = true;
                    upstream.cancel();
                    worker.dispose();
                    if (wip.getAndIncrement() == 0) {
                        item.lazySet(null);
                    }
                }
    
                void schedule() {
                    if (wip.getAndIncrement() == 0) {
                        worker.schedule(this);
                    }
                }
    
                @Override
                public void run() {
                    int missed = 1;
                    long e = emitted;
                    
                    for (;;) {
                        
                        long r = requested.get();
                        
                        while (e != r) {
                            if (cancelled) {
                                item.lazySet(null);
                                return;
                            }
                            
                            boolean d = done;
                            T v = item.get();
                            boolean empty = v == null;
                            
                            if (d && empty) {
                                Throwable ex = error;
                                if (ex == null) {
                                    actual.onComplete();
                                } else {
                                    actual.onError(ex);
                                }
                                worker.dispose();
                                return;
                            }
                            
                            if (empty) {
                                break;
                            }
                            
                            item.lazySet(null);
                            
                            upstream.request(1);
                            
                            actual.onNext(v);
                            
                            e++;
                        }
                        
                        if (e == r) {
                            if (cancelled) {
                                item.lazySet(null);
                                return;
                            }
                            if (done && item.get() == null) {
                                Throwable ex = error;
                                if (ex == null) {
                                    actual.onComplete();
                                } else {
                                    actual.onError(ex);
                                }
                                worker.dispose();
                                return;
                            }
                        }
                        
                        emitted = e;
                        missed = wip.addAndGet(-missed);
                        if (missed == 0) {
                            break;
                        }
                    }
                }
            }
        }
    }
