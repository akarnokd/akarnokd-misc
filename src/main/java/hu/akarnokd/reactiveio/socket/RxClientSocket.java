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
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.xml.stream.*;

import org.reactivestreams.Subscription;

import hu.akarnokd.rxjava2.Observable;
import hu.akarnokd.rxjava2.internal.queue.MpscLinkedQueue;
import hu.akarnokd.rxjava2.internal.subscriptions.EmptySubscription;
import hu.akarnokd.rxjava2.plugins.RxJavaPlugins;
import hu.akarnokd.rxjava2.subscribers.SerializedSubscriber;
import hu.akarnokd.xml.XElement;

public final class RxClientSocket {
    final String endpoint;
    final int port;
    
    public RxClientSocket(String endpoint, int port) {
        this.endpoint = endpoint;
        this.port = port;
    }

    public <T> Observable<T> retrieve(Function<XElement, T> unmarshaller) {
        return Observable.create(child -> {
            
            SerializedSubscriber<T> s = new SerializedSubscriber<>(child);

            boolean subscriptionSet = false;
            
            try {
                Socket socket;
                InputStream in;
                OutputStream out;
                
                socket = new Socket(endpoint, port);
                in = socket.getInputStream();
                out = socket.getOutputStream();
    
                Subscription conn = new Subscription() {
                    final MpscLinkedQueue<String> queue = new MpscLinkedQueue<>();
                    final AtomicInteger wip = new AtomicInteger();
                    
                    volatile boolean cancelled;
                    @Override
                    public void request(long n) {
                        if (cancelled) {
                            return;
                        }
                        queue.offer("<request n='" + n + "'/>");
                        if (wip.getAndIncrement() == 0) {
                            int missed = 1;
                            for (;;) {
                                for (;;) {
                                    if (cancelled) {
                                        queue.clear();
                                        return;
                                    }
                                    String str = queue.poll();
                                    
                                    if (str == null) {
                                        break;
                                    }
                                    try {
                                        out.write(str.getBytes());
                                    } catch (IOException ex) {
                                        cancel();
                                        queue.clear();
                                        s.onError(ex);
                                        return;
                                    }
                                }
                                
                                try {
                                    out.flush();
                                } catch (IOException ex) {
                                    cancel();
                                    queue.clear();
                                    s.onError(ex);
                                    return;
                                }

                                missed = wip.addAndGet(-missed);
                                if (missed == 0) {
                                    break;
                                }
                            }
                            
                        }
                    }
    
                    @Override
                    public void cancel() {
                        cancelled = true;
                        try {
                            socket.close();
                        } catch (IOException e) {
                            RxJavaPlugins.onError(e);
                        }
                    }
                    
                };
                
                s.onSubscribe(conn);
                subscriptionSet = true;
                
                XMLInputFactory xf = XMLInputFactory.newFactory();
                
                try {
                    XMLStreamReader xr = xf.createXMLStreamReader(in);
                    
                    xr.nextTag(); // get into response
                    
                    for (;;) {
                        int e = xr.nextTag();
                        if (e == XMLStreamReader.END_ELEMENT) {
                            break;
                        }
                        
                        XElement xe = XElement.parseXMLActiveFragment(xr);
                        
                        T v;
                        
                        try {
                            v = unmarshaller.apply(xe);
                        } catch (Throwable ex) {
                            s.onError(ex);
                            return;
                        }
                        
                        s.onNext(v);
                    }
                } catch (XMLStreamException ex) {
                    s.onError(ex);
                    return;
                }
    
                
                s.onComplete();
            } catch (IOException ex) {
                if (subscriptionSet) {
                    s.onError(ex);
                } else {
                    EmptySubscription.error(ex, s);
                }
            }
        });
    }
    
//    public <T> Observable<Void> send(Observable<T> source, Function<T, XElement> marshaller) {
//        
//    }
//    
//    public <T, R> Observable<R> query(Observable<T> source, Function<T, XElement> marshaller, Function<XElement, R> unmarshaller) {
//        
//    }
}
