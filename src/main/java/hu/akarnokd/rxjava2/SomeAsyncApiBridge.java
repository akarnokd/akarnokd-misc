package hu.akarnokd.rxjava2;

    import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.reactivestreams.*;

import io.reactivex.Flowable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import io.reactivex.internal.util.BackpressureHelper;

    public final class SomeAsyncApiBridge<T> extends Flowable<T> {

        final Function<? super Consumer<? super T>, ? extends CompletableFuture<Void>> apiInvoker;

        final AtomicBoolean once;

        public SomeAsyncApiBridge(Function<? super Consumer<? super T>, ? extends CompletableFuture<Void>> apiInvoker) {
            this.apiInvoker = apiInvoker;
            this.once = new AtomicBoolean();
        }

        @Override
        protected void subscribeActual(Subscriber<? super T> s) {
            if (once.compareAndSet(false, true)) {
                SomeAsyncApiBridgeSubscription<T> parent = new SomeAsyncApiBridgeSubscription<>(s, apiInvoker);
                s.onSubscribe(parent);
                parent.moveNext();
            } else {
                EmptySubscription.error(new IllegalStateException("Only one Subscriber allowed"), s);
            }
        }

        static final class SomeAsyncApiBridgeSubscription<T>
        extends AtomicInteger
        implements Subscription, Consumer<T>, BiConsumer<Void, Throwable> {

            /** */
            private static final long serialVersionUID = 1270592169808316333L;

            final Subscriber<? super T> downstream;

            final Function<? super Consumer<? super T>, ? extends CompletableFuture<Void>> apiInvoker;

            final AtomicInteger wip;

            final AtomicLong requested;

            final AtomicReference<CompletableFuture<Void>> task;

            static final CompletableFuture<Void> TASK_CANCELLED =
                    CompletableFuture.completedFuture(null);

            volatile T item;

            volatile boolean done;
            Throwable error;

            volatile boolean cancelled;

            long emitted;

            SomeAsyncApiBridgeSubscription(
                    Subscriber<? super T> downstream,
                    Function<? super Consumer<? super T>, ? extends CompletableFuture<Void>> apiInvoker) {
                this.downstream = downstream;
                this.apiInvoker = apiInvoker;
                this.requested = new AtomicLong();
                this.wip = new AtomicInteger();
                this.task = new AtomicReference<>();
            }

            @Override
            public void request(long n) {
                BackpressureHelper.add(requested, n);
                drain();
            }

            @Override
            public void cancel() {
                cancelled = true;
                CompletableFuture<Void> curr = task.getAndSet(TASK_CANCELLED);
                if (curr != null && curr != TASK_CANCELLED) {
                    curr.cancel(true);
                }
                if (getAndIncrement() == 0) {
                    item = null;
                }
            }

            void moveNext() {
                if (wip.getAndIncrement() == 0) {
                    do {
                        CompletableFuture<Void> curr = task.get();

                        if (curr == TASK_CANCELLED) {
                            return;
                        }

                        CompletableFuture<Void> f = apiInvoker.apply(this);

                        if (task.compareAndSet(curr, f)) {
                            f.whenComplete(this);
                        } else {
                            curr = task.get();
                            if (curr == TASK_CANCELLED) {
                                f.cancel(true);
                                return;
                            }
                        }
                    } while (wip.decrementAndGet() != 0);
                }
            }

            @Override
            public void accept(Void t, Throwable u) {
                if (u != null) {
                    error = u;
                    task.lazySet(TASK_CANCELLED);
                }
                done = true;
                drain();
            }

            @Override
            public void accept(T t) {
                item = t;
                drain();
            }

            void drain() {
                if (getAndIncrement() != 0) {
                    return;
                }

                int missed = 1;
                long e = emitted;

                for (;;) {

                    for (;;) {
                        if (cancelled) {
                            item = null;
                            return;
                        }

                        boolean d = done;
                        T v = item;
                        boolean empty = v == null;

                        if (d && empty) {
                            Throwable ex = error;
                            if (ex == null) {
                                downstream.onComplete();
                            } else {
                                downstream.onError(ex);
                            }
                            return;
                        }

                        if (empty || e == requested.get()) {
                            break;
                        }

                        item = null;

                        downstream.onNext(v);

                        e++;

                        moveNext();
                    }

                    emitted = e;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                }
            }
        }
    }
