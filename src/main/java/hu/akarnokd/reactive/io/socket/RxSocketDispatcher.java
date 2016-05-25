/*
 * Copyright 2015 David Karnok
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

package hu.akarnokd.reactive.io.socket;

import java.io.*;
import java.net.*;

import org.reactivestreams.*;

import hu.akarnokd.reactive.io.RxIODispatcher;
import hu.akarnokd.rxjava2.*;
import hu.akarnokd.rxjava2.functions.Function;
import hu.akarnokd.rxjava2.plugins.RxJavaPlugins;
import hu.akarnokd.rxjava2.schedulers.Schedulers;
import hu.akarnokd.xml.XElement;

public final class RxSocketDispatcher implements Subscriber<Socket> {
    final Scheduler socketWorker;
    
    final Function<Observable<XElement>, Observable<XElement>> transformer;
    
    public RxSocketDispatcher(Function<Observable<XElement>, Observable<XElement>> transformer) {
        this(Schedulers.io(), transformer);
    }
    
    public RxSocketDispatcher(Scheduler socketWorker, 
            Function<Observable<XElement>, Observable<XElement>> transformer) {
        this.socketWorker = socketWorker;
        this.transformer = transformer;
    }
    
    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }
    
    @Override
    public void onNext(Socket t) {
        socketWorker.scheduleDirect(() -> {
            handleSocket(t);
        });
    }
    
    @Override
    public void onError(Throwable t) {
        RxJavaPlugins.onError(t);
    }
    
    @Override
    public void onComplete() {
        // never happens
    }
    
    void handleSocket(Socket t) {
        try (
            InputStream in = new BufferedInputStream(t.getInputStream());
            OutputStream out = new BufferedOutputStream(t.getOutputStream());
        ){
            RxIODispatcher.dispatch(in, out, e -> transformer, transformer);
        } catch (Throwable ex) {
            if (ex instanceof SocketException) {
                if ("Socket closed".equals(ex.getMessage())) {
                    return;
                }
            }
            RxJavaPlugins.onError(ex);
        }
    }
}
