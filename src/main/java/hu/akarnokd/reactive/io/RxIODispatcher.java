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

package hu.akarnokd.reactive.io;

import java.io.*;

import javax.xml.stream.*;

import hu.akarnokd.io.ConcatInputStream;
import hu.akarnokd.rxjava2.Observable;
import hu.akarnokd.rxjava2.functions.Function;
import hu.akarnokd.rxjava2.subjects.UnicastSubject;
import hu.akarnokd.xml.XElement;

public enum RxIODispatcher {
    ;
    
    public static void dispatch(InputStream in, OutputStream out,
            Function<String, Function<Observable<XElement>, Observable<XElement>>> functions,
            Function<Observable<XElement>, Observable<XElement>> defaultFunction) throws IOException {
        
        Function<Observable<XElement>, Observable<XElement>> function = null;
        
        XElementWriterObserver observer = new XElementWriterObserver(out);
        
        UnicastSubject<XElement> requestElements = UnicastSubject.create();

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

                if (function == null) {
                    String fstr = xr.getAttributeValue(null, "f");
                    if (fstr == null) {
                        function = defaultFunction;
                    } else {
                        function = functions.apply(fstr);
                    }
                    
                    if (function == null) {
                        throw new IOException("Function not found");
                    }
                    
                    Observable<XElement> responseStream = function.apply(requestElements);
                    
                    responseStream.subscribe(observer);
                    
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
            throw new IOException(ex);
        }

    }
}
