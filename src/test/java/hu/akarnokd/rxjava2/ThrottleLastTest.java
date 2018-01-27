package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.*;

import io.reactivex.*;
import io.reactivex.Scheduler.Worker;
import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.schedulers.Schedulers;

public class ThrottleLastTest {
    public static void main(String[] args) throws InterruptedException {
        test("X");
        test("Y");
//        test("W");
//        test("Z");
//        test();
        Thread.sleep(10000);
    }

    private static void test(String tag) {
        System.out.println("before " + tag);
        Flowable<Integer> fa = Flowable.<Integer>generate(emitter -> emitter.onNext(1))
                .doOnSubscribe(v -> System.out.println("Sub A " + tag))
                //.compose(pingPongOn(Schedulers.computation()))
                .observeOn(Schedulers.computation())
                .throttleLast(1, TimeUnit.SECONDS, Schedulers.single())
                .doOnNext(v -> System.out.println("a: " + v + " " + tag))
                ;
        
        Flowable<Integer> fb = Flowable.<Integer>generate(emitter -> emitter.onNext(2))
                .doOnSubscribe(v -> System.out.println("Sub B " + tag))
//                .compose(pingPongOn(Schedulers.computation()))
                .observeOn(Schedulers.computation())
                .throttleLast(1, TimeUnit.SECONDS, Schedulers.single())
                .doOnNext(v -> System.out.println("b: " + v + " " + tag))
                ;
        Flowable.combineLatest(fa, fb, (a, b) -> a - b)
//                .subscribeOn(Schedulers.computation())
                .subscribe(c -> {
                    System.out.println("c: " + c + " " + tag);
                });
        System.out.println("after " + tag);
    }

    static <T> FlowableTransformer<T, T> requestObserveOn(Scheduler scheduler) {
        return f -> new RequestObserveOn<>(f, scheduler);
    }
    
    static final class RequestObserveOn<T> extends Flowable<T> {
        
        final Flowable<T> source;
        
        final Scheduler scheduler;

        RequestObserveOn(Flowable<T> source, Scheduler scheduler) {
            this.source = source;
            this.scheduler = scheduler;
        }
        
        @Override
        protected void subscribeActual(Subscriber<? super T> s) {
            source.subscribe(new RequestObserveOnSubscriber<>(s, scheduler.createWorker()));
        }
        
        static final class RequestObserveOnSubscriber<T> 
        extends AtomicLong
        implements FlowableSubscriber<T>, Subscription, Runnable {

            private static final long serialVersionUID = 3167152788131496136L;

            final Subscriber<? super T> actual;
            
            final Worker worker;
            
            final Runnable requestOne;

            Subscription upstream;
            
            volatile T item;
            Throwable error;
            volatile boolean done;
            
            long emitted;
            boolean terminated;
            
            RequestObserveOnSubscriber(Subscriber<? super T> actual, Scheduler.Worker worker) {
                this.actual = actual;
                this.worker = worker;
                this.requestOne = () -> upstream.request(1L);
            }

            @Override
            public void onSubscribe(Subscription s) {
                upstream = s;
                actual.onSubscribe(this);
                worker.schedule(requestOne);
            }
            
            @Override
            public void onNext(T t) {
                item = t;
                worker.schedule(this);
            }

            @Override
            public void onError(Throwable t) {
                error = t;
                done = true;
                worker.schedule(this);
            }

            @Override
            public void onComplete() {
                done = true;
                worker.schedule(this);
            }

            @Override
            public void run() {
                if (terminated) {
                    return;
                }
                boolean d = done;
                T v = item;
                boolean empty = v == null;
                
                if (d && empty) {
                    Throwable ex = error;
                    if (ex != null) {
                        actual.onError(ex);
                    } else {
                        actual.onComplete();
                    }
                    worker.dispose();
                    terminated = true;
                    return;
                }
                long e = emitted;
                if (!empty && e != get()) {
                    item = null;
                    actual.onNext(v);
                    emitted = e + 1;
                    worker.schedule(requestOne);
                }
            }

            @Override
            public void request(long n) {
                BackpressureHelper.add(this, n);
                worker.schedule(this);
            }

            @Override
            public void cancel() {
                upstream.cancel();
                worker.dispose();
                item = null;
            }
        }
    }
}
