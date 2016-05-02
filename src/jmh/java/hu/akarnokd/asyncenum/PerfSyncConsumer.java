/*
 * Copyright 2016 David Karnok
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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jmh.infra.Blackhole;

import rx.internal.util.unsafe.SpscUnboundedArrayQueue;
import rx.subscriptions.CompositeSubscription;

/**
 * Consumes a synchronous IAsyncEnumerator without blocking.
 */
public final class PerfSyncConsumer
extends AtomicInteger {
    /** */
    private static final long serialVersionUID = 1938738165002103743L;

    final Blackhole bh;
    
    final IAsyncEnumerator<?> source;
    
    final Queue<CompletionStage<Boolean>> queue;
    
    final CompositeSubscription csub;
    
    public PerfSyncConsumer(Blackhole bh, IAsyncEnumerator<?> source) {
        this.bh = bh;
        this.source = source;
        this.queue = new SpscUnboundedArrayQueue<>(16);
        this.csub = new CompositeSubscription();
    }
    
    public void consume() {
        consumeStage(source.moveNext(csub));
    }
    
    void consumeStage(CompletionStage<Boolean> stage) {
        Queue<CompletionStage<Boolean>> q = queue;
        
        q.offer(stage);
        
        if (getAndIncrement() == 0) {
            do {
                stage = q.poll();
                
                stage.whenComplete((b, e) -> {
                    if (e != null) {
                        bh.consume(e);
                    } else
                    if (b) {
                        bh.consume(source.current());
                        consumeStage(source.moveNext(csub));
                    } else {
                        bh.consume(false);
                    }
                });
            } while (decrementAndGet() != 0);
        }
    }
}
