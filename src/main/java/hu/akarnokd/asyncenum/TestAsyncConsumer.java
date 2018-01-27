/*
 * Copyright 2016-2018 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package hu.akarnokd.asyncenum;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import rx.internal.util.atomic.SpscLinkedArrayQueue;
import rx.observers.TestSubscriber;
import rx.subscriptions.CompositeSubscription;

public final class TestAsyncConsumer<T> {

    final TestSubscriber<T> ts;

    final IAsyncEnumerator<? extends T> source;

    final CompositeSubscription csub;

    final Queue<CompletionStage<Boolean>> queue;

    final AtomicInteger wip;

    public TestAsyncConsumer(IAsyncEnumerator<? extends T> source) {
        this.source = source;
        this.csub = new CompositeSubscription();
        this.ts = new TestSubscriber<>();
        this.wip = new AtomicInteger();
        this.queue = new SpscLinkedArrayQueue<>(16);
    }

    void consumeStageAll(CompletionStage<Boolean> stage) {
        queue.offer(stage);
        if (wip.getAndIncrement() == 0) {
            drainLoop();
        }
    }

    void drainLoop() {
        do {
            CompletionStage<Boolean> stage = queue.poll();
            stage
            .whenComplete((b, e) -> {

                if (csub.isUnsubscribed()) {
                    return;
                }
                if (e != null) {
                    ts.onError(e);
                } else
                if (b) {
                    ts.onNext(source.current());
                    consumeStageAll(source.moveNext(csub));
                } else {
                    ts.onCompleted();
                }
            });
        } while (wip.decrementAndGet() != 0);
    }

//    void consumeStage(CompletionStage<Boolean> stage, long n, CountDownLatch cdl) {
//        stage
//        .whenComplete((b, e) -> {
//            if (csub.isUnsubscribed()) {
//                return;
//            }
//            if (e != null) {
//                ts.onError(e);
//                cdl.countDown();
//            } else
//            if (b) {
//                ts.onNext(source.current());
//                if (n == 0) {
//                    cdl.countDown();
//                } else {
//                    consumeStage(source.moveNext(csub), n - 1, cdl);
//                }
//            } else {
//                ts.onCompleted();
//                cdl.countDown();
//            }
//        });
//    }

//    TestAsyncConsumer<T> consumeAwait(long n) {
//        CountDownLatch cdl = new CountDownLatch(1);
//
//        consumeStage(source.moveNext(csub), n, cdl);
//
//        try {
//            cdl.await();
//        } catch (InterruptedException ex) {
//            throw new RuntimeException(ex);
//        }
//
//        return this;
//    }

    public TestAsyncConsumer<T> consumeAwaitAll() {
        consumeAwaitAll();
        return this;
    }

//    TestAsyncConsumer<T> consumeAwait(long n, long timeout, TimeUnit unit) {
//        CountDownLatch cdl = new CountDownLatch(1);
//
//        consumeStage(source.moveNext(csub), n, cdl);
//
//        try {
//            if (!cdl.await(timeout, unit)) {
//                csub.unsubscribe();
//                throw new AssertionError("TestAsyncConsumer timed out: values received: " + ts.getOnNextEvents().size());
//            }
//        } catch (InterruptedException ex) {
//            throw new RuntimeException(ex);
//        }
//
//        return this;
//    }

    public TestAsyncConsumer<T> consumeAwaitAll(long timeout, TimeUnit unit) {
        consumeStageAll(source.moveNext(csub));

        ts.awaitTerminalEvent(timeout, unit);
//        if (ts.getCompletions() == 0 && ts.getOnErrorEvents().isEmpty()) {
//            csub.unsubscribe();
//            throw new AssertionError("TestAsyncConsumer timed out: values received: " + ts.getOnNextEvents().size());
//        }

        return this;
    }

    public TestAsyncConsumer<T> assertValue(T value) {
        ts.assertValue(value);
        return this;
    }

    @SafeVarargs
    public final TestAsyncConsumer<T> assertValues(T... values) {
        ts.assertValues(values);
        return this;
    }

    public TestAsyncConsumer<T> assertNoValues() {
        ts.assertNoValues();
        return this;
    }

    public TestAsyncConsumer<T> assertValueCount(int count) {
        ts.assertValueCount(count);
        return this;
    }

    public TestAsyncConsumer<T> assertNoErrors() {
        ts.assertNoErrors();
        return this;
    }

    public TestAsyncConsumer<T> assertCompleted() {
        ts.assertCompleted();
        return this;
    }

    public TestAsyncConsumer<T> assertNotCompleted() {
        ts.assertNotCompleted();
        return this;
    }
}
