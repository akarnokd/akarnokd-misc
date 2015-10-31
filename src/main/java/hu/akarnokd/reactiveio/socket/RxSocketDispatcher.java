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
import java.util.function.Function;

import javax.xml.stream.*;

import org.reactivestreams.*;

import hu.akarnokd.rxjava2.*;
import hu.akarnokd.rxjava2.plugins.RxJavaPlugins;
import hu.akarnokd.rxjava2.schedulers.Schedulers;
import hu.akarnokd.rxjava2.subjects.UnicastSubject;
import hu.akarnokd.rxjava2.subscribers.AsyncObserver;
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
            
            XElementWriterObserver observer = new XElementWriterObserver(out);
            
            try {
                UnicastSubject<XElement> requestElements = UnicastSubject.create();
                
                Observable<XElement> responseStream = transformer.apply(requestElements);
                
                responseStream.subscribe(observer);
                
                parseXMLRequest(in, out, observer, requestElements);
            } catch (IOException ex) {
                throw ex;
            } catch (Throwable ex) {
                observer.onError(ex);
            }
        } catch (Throwable ex) {
            if (ex instanceof SocketException) {
                if ("Socket closed".equals(ex.getMessage())) {
                    return;
                }
            }
            RxJavaPlugins.onError(ex);
        }
    }

    static void parseXMLRequest(InputStream in, OutputStream out, 
            XElementWriterObserver observer,
            Subscriber<XElement> requestElements)
                    throws FactoryConfigurationError, XMLStreamException, IOException {
        XMLInputFactory xf = XMLInputFactory.newFactory();

        try (ConcatInputStream cin = new ConcatInputStream(
                new ByteArrayInputStream("<requests>\n".getBytes()), 
                in, 
                new ByteArrayInputStream("\n</requests>".getBytes()));) {
        
            XMLStreamReader xr = xf.createXMLStreamReader(cin);
    
            xr.nextTag(); // get into requests
            
            for (;;) {
                // should be a request node
                int e = xr.nextTag();
                
                if (e == XMLStreamReader.END_ELEMENT) {
                    break;
                }

                if (!xr.getLocalName().equals("request")) {
                    requestElements.onError(new XMLStreamException("Wrong element, request expected but got " + xr.getLocalName()));
                    return;
                }

                String nstr = xr.getAttributeValue(null, "n");
                if (nstr != null) {
                    long n = Long.parseLong(nstr);
                    if (n < 0) {
                        requestElements.onComplete();
                    } else {
                        observer.requestMore(n);
                    }
                }
                
                e = xr.nextTag();
                
                if (e == XMLStreamReader.START_ELEMENT) {
                    for (;;) {
                        XElement xe = XElement.parseXMLActiveFragment(xr);
                        
                        requestElements.onNext(xe);
                        
                        e = xr.nextTag();
                        if (e == XMLStreamReader.END_ELEMENT) {
                            break;
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Throwable nestedException = ex.getNestedException();
            if (nestedException instanceof IOException) {
                if (observer.completing.get() 
                        || "Socket closed".equals(nestedException.getMessage())
                        || "Software caused connection abort: recv failed".equals(nestedException.getMessage())) {
                    return;
                }
            }
            throw ex;
        }
    }
    
    static final class XElementWriterObserver extends AsyncObserver<XElement> {
        final OutputStream out;
        final AtomicBoolean completing;
        
        public XElementWriterObserver(OutputStream out) {
            this.out = out;
            this.completing = new AtomicBoolean();
        }
        
        @Override
        protected void onStart() {
            try {
                out.write("<?xml version='1.0' encoding='UTF-8'?>".getBytes("UTF-8"));
                out.write(String.format("<response>%n").getBytes("UTF-8"));
            } catch (IOException ex) {
                RxJavaPlugins.onError(ex);
                cancel();
            }
        }
        
        @Override
        public void onNext(XElement t) {
            try {
                t.save(out, false, true);
            } catch (IOException ex) {
                RxJavaPlugins.onError(ex);
                cancel();
            } catch (Throwable ex) {
                RxJavaPlugins.onError(ex);
                cancel();
                onComplete();
            }
        }
        
        @Override
        public void onError(Throwable t) {
            RxJavaPlugins.onError(t);
            onComplete();
        }
        
        @Override
        public void onComplete() {
            if (completing.compareAndSet(false, true)) {
                try {
                    out.write(String.format("</response>%n").getBytes("UTF-8"));
                } catch (IOException ex) {
                    RxJavaPlugins.onError(ex);
                }
                try {
                    out.flush();
                } catch (IOException ex) {
                    // ignoring flush-termination races
                }
            }
        }
        
        public void requestMore(long n) {
            request(n);
        }
    }
}
