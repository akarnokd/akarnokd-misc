package hu.akarnokd.reactiveio.socket;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
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
                out.write(("<response error='Exception: " 
                        + XElement.sanitize(ex.toString())
                        + "'/>").getBytes(Charset.forName("UTF-8")));
            }
        } catch (Throwable ex) {
            RxJavaPlugins.onError(ex);
        }
    }

    static void parseXMLRequest(InputStream in, OutputStream out, 
            XElementWriterObserver observer,
            Subscriber<XElement> requestElements)
                    throws FactoryConfigurationError, XMLStreamException, IOException {
        XMLInputFactory xf = XMLInputFactory.newFactory();

        for (;;) {
            // check if we reached the end of stream
            in.mark(1);
            
            if (in.read() < 0) {
                break;
            }
            
            in.reset();
            
            // since we expect multiple <request> elements, we have to read them by fragment 
            XMLStreamReader xr = xf.createXMLStreamReader(in);
            
            int e = -1;
            while (xr.hasNext()) {
                e = xr.next();
                
                if (e == XMLStreamReader.START_ELEMENT) { 
                    break;
                }
                if (e == XMLStreamReader.END_ELEMENT
                        || e == XMLStreamReader.END_DOCUMENT) {
                    return;
                }
            }
     
            if (e == -1) {
                break;
            }
            
            if (!"request".equals(xr.getLocalName())) {
                out.write("<response error='Protocol error'/>".getBytes(Charset.forName("ISO-8859-1")));
                return;
            }

            long n = Long.parseLong(xr.getAttributeValue(null, "n"));
            
            observer.requestMore(n);
            
            if (xr.getAttributeValue(null, "hasBody") != null) {
                // keep processing the internals of "request"
                while (xr.hasNext()) {
                    XElement xe = XElement.parseXMLFragment(xr);
                    requestElements.onNext(xe);
                }
            } else {
                while (xr.hasNext() && xr.next() != XMLStreamReader.END_DOCUMENT);
            }
        }
    }
    
    static final class XElementWriterObserver extends AsyncObserver<XElement> {
        final OutputStream out;
        
        public XElementWriterObserver(OutputStream out) {
            this.out = out;
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
                t.save(out);
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
            try {
                out.write(String.format("</response>%n").getBytes("UTF-8"));
                out.close();
            } catch (IOException ex) {
                RxJavaPlugins.onError(ex);
            }
        }
        
        public void requestMore(long n) {
            request(n);
        }
    }
}
