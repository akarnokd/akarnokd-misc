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

import org.junit.Test;

import hu.akarnokd.reactiveio.socket.RxSocketDispatcher.XElementWriterObserver;
import hu.akarnokd.rxjava2.subjects.UnicastSubject;
import hu.akarnokd.xml.XElement;

public class RxSocketDispatcherTest {
    @Test
    public void singleEmptyRequestParsing() throws Exception {
        
        InputStream in = new ByteArrayInputStream("<request n='1'/>".getBytes());
        
        OutputStream out = new ByteArrayOutputStream();
        
        XElementWriterObserver observer = new XElementWriterObserver(out);
        
        RxSocketDispatcher.parseXMLRequest(in, out, observer , UnicastSubject.create());
        
    }

    @Test
    public void singlePayloadRequestParsing() throws Exception {
        
        InputStream in = new ByteArrayInputStream("<request n='1'><integer>2</integer></request>".getBytes());
        
        OutputStream out = new ByteArrayOutputStream();
        
        XElementWriterObserver observer = new XElementWriterObserver(out);
        
        UnicastSubject<XElement> create = UnicastSubject.create();
        
        create.subscribe(System.out::println, Throwable::printStackTrace);
        
        RxSocketDispatcher.parseXMLRequest(in, out, observer , create);
        
    }

    
    @Test
    public void manyEmptyRequestParsing() throws Exception {
        
        InputStream in = new ByteArrayInputStream("<request n='1'/><request n='1'/><request n='1'/>".getBytes());
        
        OutputStream out = new ByteArrayOutputStream();
        
        XElementWriterObserver observer = new XElementWriterObserver(out);
        
        RxSocketDispatcher.parseXMLRequest(in, out, observer , UnicastSubject.create());
        
    }

    @Test
    public void manyPayloadRequestParsing() throws Exception {
        
        InputStream in = new ByteArrayInputStream(("<request n='1'><integer>20</integer></request>"
                + "<request n='1'><integer>21</integer></request>").getBytes());
        
        OutputStream out = new ByteArrayOutputStream();
        
        XElementWriterObserver observer = new XElementWriterObserver(out);
        
        UnicastSubject<XElement> create = UnicastSubject.create();
        
        create.subscribe(System.out::println, Throwable::printStackTrace);
        
        RxSocketDispatcher.parseXMLRequest(in, out, observer , create);
        
    }

    @Test
    public void batchPayloadRequestParsing() throws Exception {
        
        InputStream in = new ByteArrayInputStream(("<request n='1'><integer>4</integer>"
                + "<integer>5</integer></request>").getBytes());
        
        OutputStream out = new ByteArrayOutputStream();
        
        XElementWriterObserver observer = new XElementWriterObserver(out);
        
        UnicastSubject<XElement> create = UnicastSubject.create();
        
        create.subscribe(System.out::println, Throwable::printStackTrace);
        
        RxSocketDispatcher.parseXMLRequest(in, out, observer , create);
        
    }

}
