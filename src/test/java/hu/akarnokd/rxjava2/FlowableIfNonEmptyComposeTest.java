package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.*;
import java.util.function.Function;

import org.junit.Test;
import org.reactivestreams.*;

import io.reactivex.*;
import io.reactivex.subscribers.TestSubscriber;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.*;
import reactor.util.concurrent.Queues;

public class FlowableIfNonEmptyComposeTest {

    @Test
    public void simple() throws Exception {
        Flux.range(1, 10)
        .hide()
        .compose(composeIfNonEmpty(f -> {
            System.out.println("Composed!");
            return f.doOnNext(System.out::println).then();
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void simple2() {
        Function<Flux<Integer>, Flux<Integer>> transform = g -> g.doOnNext(System.out::println);
        Mono<Integer> source = Flux.range(1, 5)
                .publish(f ->
                    f.limitRequest(1)
                        .concatMap(first -> transform.apply(f.startWith(first)))
                )
                .ignoreElements()
        ;

        source.subscribeWith(new TestSubscriber<Integer>())
        .assertResult();
    }
    
    @Test
    public void error() {
        Flux.error(new IOException())
        .compose(composeIfNonEmpty(f -> {
            System.out.println("Composed!");
            return Mono.from(f.doOnNext(System.out::println));
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }


    @Test
    public void simpleDedicated() throws Exception {
        Flux.range(1, 10)
        .hide()
        .compose(composeIfNonEmptyDedicated(f -> {
            System.out.println("Composed!");
            return f.doOnNext(System.out::println).then();
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void errorDedicated() {
        Flux.error(new IOException())
        .compose(composeIfNonEmptyDedicated(f -> {
            System.out.println("Composed!");
            return Mono.from(f.doOnNext(System.out::println));
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void simpleRx() {
        Flowable.range(1, 10)
        .doOnNext(v -> {
            System.out.println(">> " + v);
        })
        .compose(composeIfNonEmptyRx(f -> {
            System.out.println("Composed!");
            return f.doOnNext(System.out::println).ignoreElements().<Void>toFlowable();
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void errorRx() {
        Flowable.error(new IOException())
        .compose(composeIfNonEmptyRx(f -> {
            System.out.println("Composed!");
            return f.doOnNext(System.out::println).ignoreElements().toFlowable();
        }))
        .subscribe(v -> { }, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    static <T> Function<Flux<T>, Mono<Void>> composeIfNonEmpty(Function<? super Flux<T>, ? extends Mono<Void>> f) {
        return g ->
            g.publish(h -> 
                h.limitRequest(1).concatMap(first -> f.apply(h.startWith(first)))
            ).ignoreElements();
    }

    static <T> FlowableTransformer<T, Void> composeIfNonEmptyRx(Function<? super Flowable<T>, ? extends Flowable<Void>> f) {
        return g ->
            g.publish(h -> 
                h.limit(1).concatMap(first -> f.apply(h.startWith(first)))
            ).ignoreElements().toFlowable();
    }

    static <T> Function<Flux<T>, Mono<Void>> composeIfNonEmptyDedicated(Function<? super Flux<T>, ? extends Mono<Void>> f) {
        return new Function<Flux<T>, Mono<Void>>() {
            @Override
            public Mono<Void> apply(Flux<T> g) {
                return new Mono<Void>() {

                    @Override
                    public void subscribe(CoreSubscriber<? super Void> actual) {
                        g.subscribe(new ComposeIfNotEmptySubscriber<>(actual, f, Queues.SMALL_BUFFER_SIZE));
                    }
                };
            }
        };
    }

    static final class ComposeIfNotEmptySubscriber<T>
    extends Flux<T>
    implements CoreSubscriber<T>, Subscription {

        final Subscriber<? super Void> outputMonoSubscriber;
        
        final Function<? super Flux<T>, ? extends Mono<Void>> transformer;
        
        final Queue<T> queue;

        final AtomicReference<TransformSubscription<T>> transformProducer;

        final TransformConsumer<T> transformConsumer;
        
        static final TransformSubscription<Object> TERMINATED = new TransformSubscription<>(null, null);

        final int prefetch;
        
        final AtomicInteger wip;

        Subscription upstream;

        volatile boolean done;
        Throwable error;

        volatile boolean cancelled;
        
        int consumed;
        
        long emitted;

        boolean nonEmpty;

        ComposeIfNotEmptySubscriber(Subscriber<? super Void> outputMonoSubscriber, Function<? super Flux<T>, ? extends Mono<Void>> transformer, int prefetch) {
            this.outputMonoSubscriber = outputMonoSubscriber;
            this.transformer = transformer;
            this.queue = Queues.<T>small().get();
            this.prefetch = prefetch;
            this.transformProducer = new AtomicReference<>();
            this.wip = new AtomicInteger();
            this.transformConsumer = new TransformConsumer<>(this);
        }

        @Override
        public void onSubscribe(Subscription s) {
            upstream = s;
            outputMonoSubscriber.onSubscribe(this);
            s.request(1);
        }
        
        @Override
        public void onNext(T t) {
            if (!nonEmpty) {
                nonEmpty = true;
                if (prefetch != 1) {
                    upstream.request(prefetch - 1);
                }
                
                try {
                    transformer.apply(this).subscribe(transformConsumer);
                } catch (Throwable ex) {
                    cancel();
                    onError(ex);
                    return;
                }
            }
            queue.offer(t);
            drain();
        }
        
        @Override
        public void onError(Throwable t) {
            upstream = Operators.cancelledSubscription();
            if (!nonEmpty) {
                outputMonoSubscriber.onError(t);
            } else {
                error = t;
                done = true;
                drain();
            }
        }
        
        @Override
        public void onComplete() {
            upstream = Operators.cancelledSubscription();
            if (!nonEmpty) {
                outputMonoSubscriber.onComplete();
            } else {
                done = true;
                drain();
            }
        }

        @Override
        public void request(long n) {
            // the mono output ignores items anyway
        }

        @Override
        public void cancel() {
            cancelled = true;
            upstream.cancel();
            transformConsumer.cancel();
            if (wip.getAndIncrement() == 0) {
                queue.clear();
            }
        }
        
        @Override
        public void subscribe(CoreSubscriber<? super T> actual) {
            TransformSubscription<T> sub = new TransformSubscription<>(actual, this);
            actual.onSubscribe(sub);
            if (transformProducer.compareAndSet(null, sub)) {
                if (sub.cancelled) {
                    transformProducer.compareAndSet(sub, null);
                    return;
                }
                drain();
            } else {
                if (transformProducer.get() == TERMINATED) {
                    Throwable ex = error;
                    if (ex == null) {
                        actual.onComplete();
                    } else {
                        actual.onError(ex);
                    }
                } else {
                    actual.onError(new IllegalStateException("Only one Subscriber allowed at once"));
                }
            }
        }
        
        @SuppressWarnings({"unchecked", "rawtypes"})
        void drain() {
            if (wip.getAndIncrement() != 0) {
                return;
            }
            
            int missed = 1;
            long e = emitted;
            int c = consumed;
            int lim = prefetch - (prefetch >> 2);

            TransformSubscription<T> current = transformProducer.get();

            outer:
            for (;;) {
                
                if (current != null && !current.cancelled) {
                    long r = current.get();
                    
                    while (e != r) {
                        if (cancelled) {
                            queue.clear();
                            return;
                        }

                        if (current.cancelled) {
                            break;
                        }
                        
                        boolean d = done;
                        T v = queue.poll();
                        boolean empty = v == null;
                        
                        if (d && empty) {
                            Throwable ex = error;
                            current = transformProducer.getAndSet((TransformSubscription)TERMINATED);
                            if (current != null) {
                                if (ex == null) {
                                    current.transformSubscriber.onComplete();
                                } else {
                                    current.transformSubscriber.onError(ex);
                                }
                            }
                            return;
                        }
                        
                        if (empty) {
                            break;
                        }

                        current.transformSubscriber.onNext(v);

                        e++;
                        
                        if (++c == lim) {
                            c = 0;
                            upstream.request(lim);
                        }
                        
                        TransformSubscription<T> fresh = transformProducer.get();
                        if (current.cancelled || current != fresh) {
                            current = fresh;
                            continue outer;
                        }
                    }
                    
                    if (e == r) {
                        if (cancelled) {
                            queue.clear();
                            return;
                        }
                        
                        boolean d = done;
                        boolean empty = queue.isEmpty();
                        
                        if (d && empty) {
                            Throwable ex = error;
                            current = transformProducer.getAndSet((TransformSubscription)TERMINATED);
                            if (current != null) {
                                if (ex == null) {
                                    current.transformSubscriber.onComplete();
                                } else {
                                    current.transformSubscriber.onError(ex);
                                }
                            }
                            return;
                        }
                    }
                }
                
                missed = wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
                
                current = transformProducer.get();
            }
        }
        
        static final class TransformSubscription<T> extends AtomicLong
        implements Subscription {

            private static final long serialVersionUID = -6104290236755265132L;

            final Subscriber<? super T> transformSubscriber;

            final ComposeIfNotEmptySubscriber<T> parent;
            
            volatile boolean cancelled;

            TransformSubscription(Subscriber<? super T> transformSubscriber, ComposeIfNotEmptySubscriber<T> parent) {
                this.transformSubscriber = transformSubscriber;
                this.parent = parent;
            }
            
            @Override
            public void request(long n) {
                for (;;) {
                    long a = get();
                    if (a == Long.MAX_VALUE) {
                        break;
                    }
                    long b = Operators.addCap(a, n);
                    if (compareAndSet(a, b)) {
                        parent.drain();
                        break;
                    }
                }
                
            }
            
            @Override
            public void cancel() {
                cancelled = true;
                parent.transformProducer.compareAndSet(this, null);
            }
        }
        
        static final class TransformConsumer<T> 
        extends AtomicReference<Subscription>
        implements CoreSubscriber<Void> {

            private static final long serialVersionUID = -3146687172085237026L;

            final ComposeIfNotEmptySubscriber<T> parent;
            
            TransformConsumer(ComposeIfNotEmptySubscriber<T> parent) {
                this.parent = parent;
            }
            
            @Override
            public void onSubscribe(Subscription s) {
                if (compareAndSet(null, s)) {
                    s.request(Long.MAX_VALUE);
                } else {
                    s.cancel();
                }
            }
            
            @Override
            public void onNext(Void t) {
            }
            
            @Override
            public void onError(Throwable t) {
                parent.upstream.cancel();
                parent.outputMonoSubscriber.onError(t);
            }

            @Override
            public void onComplete() {
                parent.upstream.cancel();
                parent.outputMonoSubscriber.onComplete();
            }
            
            void cancel() {
                Subscription s = getAndSet(Operators.cancelledSubscription());
                if (s != null && s != Operators.cancelledSubscription()) {
                    s.cancel();
                }
            }
        }
    }
}
