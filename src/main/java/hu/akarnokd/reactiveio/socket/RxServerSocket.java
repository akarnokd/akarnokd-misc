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

package hu.akarnokd.reactiveio.socket;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

import hu.akarnokd.rxjava2.*;
import hu.akarnokd.rxjava2.plugins.RxJavaPlugins;
import hu.akarnokd.rxjava2.schedulers.Schedulers;
import hu.akarnokd.rxjava2.subjects.UnicastSubject;

public final class RxServerSocket implements Closeable {

    ServerSocket socket;
    final Scheduler waiter;
    
    final AtomicBoolean acceptOnce;
    final AtomicBoolean closeOnce;
    
    final UnicastSubject<Socket> incomingSockets;
    
    public RxServerSocket() throws IOException {
        this(Schedulers.io());
    }

    public RxServerSocket(Scheduler waiter) throws IOException {
        this.socket = new ServerSocket();
        this.waiter = waiter;
        this.acceptOnce = new AtomicBoolean();
        this.closeOnce = new AtomicBoolean();
        this.incomingSockets = UnicastSubject.create(Observable.bufferSize(), () -> {
            if (closeOnce.compareAndSet(false, true)) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    RxJavaPlugins.onError(ex);
                }
            }
        });
    }
    
    public void bind(int port) throws IOException {
        socket.bind(new InetSocketAddress(port));
    }
    
    public Observable<Socket> accept() {
        if (acceptOnce.compareAndSet(false, true)) {
            waiter.scheduleDirect(this::acceptLoop);
        }
        return incomingSockets;
    }
    
    protected void acceptLoop() {
        UnicastSubject<Socket> subject = incomingSockets;
        try {
            for (;;) {
                subject.onNext(socket.accept());
            }
        } catch (IOException ex) {
            if (!closeOnce.get()) {
                subject.onError(ex);
            }
        }
    }
    
    @Override
    public void close() throws IOException {
        if (closeOnce.compareAndSet(false, true)) {
            socket.close();
        }
    }
}
