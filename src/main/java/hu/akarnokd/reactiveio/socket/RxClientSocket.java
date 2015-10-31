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
import java.util.Queue;
import java.util.concurrent.atomic.*;

import javax.xml.stream.*;

import org.reactivestreams.*;

import hu.akarnokd.rxjava2.Observable;
import hu.akarnokd.rxjava2.functions.Function;
import hu.akarnokd.rxjava2.internal.queue.MpscLinkedQueue;
import hu.akarnokd.rxjava2.internal.subscriptions.EmptySubscription;
import hu.akarnokd.rxjava2.internal.util.BackpressureHelper;
import hu.akarnokd.rxjava2.plugins.RxJavaPlugins;
import hu.akarnokd.rxjava2.subscribers.SerializedSubscriber;
import hu.akarnokd.xml.XElement;

public final class RxClientSocket {
    private static final byte[] REQUEST_N_POSTFIX = "'/>".getBytes();
    private static final byte[] REQUEST_N_PREFIX = "<request n='".getBytes();
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
            
            try (
                Socket socket = new Socket(endpoint, port);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                    ) {
                Subscription conn = new Subscription() {
                    final AtomicInteger wip = new AtomicInteger();
                    final AtomicLong missedRequested = new AtomicLong();
                    
                    volatile boolean cancelled;
                    @Override
                    public void request(long n) {
                        if (cancelled) {
                            return;
                        }
                        BackpressureHelper.add(missedRequested, n);
                        if (wip.getAndIncrement() == 0) {
                            int missed = 1;
                            for (;;) {
                                for (;;) {
                                    if (cancelled) {
                                        return;
                                    }
                                    long r = missedRequested.getAndSet(0L);
                                    
                                    if (r == 0L) {
                                        break;
                                    }
                                    try {
                                        out.write(REQUEST_N_PREFIX);
                                        out.write(Long.toString(r).getBytes());
                                        out.write(REQUEST_N_POSTFIX);
                                    } catch (IOException ex) {
                                        cancel();
                                        s.onError(ex);
                                        return;
                                    }
                                }
                                
                                try {
                                    out.flush();
                                } catch (IOException ex) {
                                    cancel();
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
    
    public <T> Observable<Void> send(Observable<T> source, Function<? super T, XElement> marshaller) {
        return Observable.create(child -> {
            SerializedSubscriber<Void> s = new SerializedSubscriber<>(child);

            boolean subscriptionSet = false;
            
            try (
                Socket socket = new Socket(endpoint, port);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                    ) {

                
                s.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        // ignored
                    }

                    @Override
                    public void cancel() {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            
                        }
                    }
                    
                });
                
                final AtomicInteger wip = new AtomicInteger(2);
                
                source
                .map(marshaller)
                .subscribe(new Subscriber<XElement>() {
                    Subscription sr;
                    @Override
                    public void onSubscribe(Subscription sr) {
                        this.sr = sr;
                        try {
                            out.write(("<request n='" + Long.MAX_VALUE + "'>").getBytes());
                            out.flush();
                        } catch (IOException ex) {
                            cancel();
                            s.onError(ex);
                            return;
                        }
                        sr.request(Long.MAX_VALUE);
                    }

                    void cancel() {
                        sr.cancel();
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            
                        }
                    }
                    
                    @Override
                    public void onNext(XElement t) {
                        try {
                            t.save(out, false, true);
                        } catch (IOException e) {
                            cancel();
                            s.onError(e);
                        }
                    }
                    

                    @Override
                    public void onError(Throwable t) {
                        cancel();
                        s.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        try {
                            out.write("</request>".getBytes());
                        } catch (IOException ex) {
                            s.onError(ex);
                        }
                        try {
                            if (wip.decrementAndGet() == 0) {
                                out.close();
                            } else {
                                out.flush();
                            }
                        } catch (IOException ex) {
                            
                        }
                    }
                    
                });
                
                
                byte[] buffer = new byte[4096];
                
                for (;;) {
                    int r = in.read(buffer);
                    if (r < 0) {
                        break;
                    }
                }
                s.onComplete();
                if (wip.decrementAndGet() == 0) {
                    socket.close();
                }
            } catch (IOException ex) {
                if (subscriptionSet) {
                    s.onError(ex);
                } else {
                    EmptySubscription.error(ex, s);
                }
            }
        });
    }
    
    public <T, R> Observable<R> map(Observable<T> source, Function<T, XElement> marshaller, Function<XElement, R> unmarshaller) {
        return Observable.create(child -> {
            SerializedSubscriber<R> s = new SerializedSubscriber<>(child);

            boolean subscriptionSet = false;
            
            try (
                Socket socket = new Socket(endpoint, port);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                    ) {

                InOutSubscriber inout = new InOutSubscriber(s, socket, out);
                
                s.onSubscribe(inout);
                subscriptionSet = true;

                source
                .map(marshaller)
                .subscribe(inout);

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
                        
                        R v;
                        
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
                    socket.close();
                    return;
                }
    
                
                s.onComplete();
                inout.done();
            } catch (IOException ex) {
                if (subscriptionSet) {
                    s.onError(ex);
                } else {
                    EmptySubscription.error(ex, s);
                }
            }
        });
    }
    
    static final class InOutSubscriber implements Subscriber<XElement>, Subscription {
        final AtomicInteger wip = new AtomicInteger();
        final AtomicLong missedRequested = new AtomicLong();
        final AtomicInteger close = new AtomicInteger(2);
        final Socket socket;
        final OutputStream out;
        final Subscriber<?> s;
        volatile boolean cancelled;
        volatile boolean done;
        
        boolean outClose;

        Subscription sr;
        
        final Queue<Object> queue;

        public InOutSubscriber(Subscriber<?> s, Socket socket, OutputStream out) {
            this.s = s;
            this.socket = socket;
            this.out = out;
            this.queue = new MpscLinkedQueue<>();
            this.queue.offer("<request>");
            drain();
        }
        
        @Override
        public void request(long n) {
            if (cancelled) {
                return;
            }
            BackpressureHelper.add(missedRequested, n);
            drain();
        }

        @Override
        public void onSubscribe(Subscription sr) {
            this.sr = sr;
            drain();
            sr.request(Long.MAX_VALUE);
        }

        @Override
        public void cancel() {
            cancelled = true;
            sr.cancel();
            try {
                socket.close();
            } catch (IOException ex) {
                
            }
        }
        
        @Override
        public void onNext(XElement t) {
            queue.offer(t);
            drain();
        }
        

        @Override
        public void onError(Throwable t) {
            cancel();
            s.onError(t);
        }

        @Override
        public void onComplete() {
            queue.offer("</request>");
            done = true;
            drain();
        }
        
        void drain() {
            if (wip.getAndIncrement() != 0) {
                return;
            }
            int missed = 1;
            for (;;) {
                try {
                    for (;;) {
                        if (cancelled) {
                            return;
                        }
                        
                        boolean d = done;
                        Object o = queue.poll();
                        boolean empty = o == null;
                        
                        if (d && empty) {
                            if (!outClose) {
                                outClose = true;
                                if (close.decrementAndGet() == 0) {
                                    out.close();
                                    return;
                                }
                            }
                            long r = missedRequested.get();
                            if (r != 0) {
                                out.write("<request n='".getBytes());
                                out.write(Long.toString(r).getBytes());
                                out.write("'/>".getBytes());
                            }
                        } else {
                            long r = missedRequested.get();
                            if (r != 0) {
                                r = missedRequested.getAndSet(0L);
                                out.write("</request>".getBytes());
                                out.write("<request n='".getBytes());
                                out.write(Long.toString(r).getBytes());
                                out.write("'>".getBytes());
                            }
                        }
                            
                        if (empty) {
                            break;
                        }
                        
                        if (o instanceof XElement) {
                            XElement xe = (XElement) o;
                            xe.save(out, false, false);
                        } else
                        if (o instanceof String) {
                            out.write(((String)o).getBytes("UTF-8"));
                        }
                        
                    }
                    
                    if (cancelled) {
                        return;
                    }

                    out.flush();

                    missed = wip.addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } catch (IOException ex) {
                    if (!cancelled) {
                        cancel();
                        s.onError(ex);
                    }
                    return;
                }
            }
        }
        
        void done() {
            if (close.decrementAndGet() == 0) {
                try {
                    out.close();
                } catch (IOException ex) {
                    
                }
            }
        }
    }
}
