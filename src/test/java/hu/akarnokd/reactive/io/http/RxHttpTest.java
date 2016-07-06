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

package hu.akarnokd.reactive.io.http;

import java.net.URL;
import java.util.*;

import org.junit.Test;

import hu.akarnokd.rxjava2.Observable;
import hu.akarnokd.rxjava2.functions.Function;
import hu.akarnokd.rxjava2.schedulers.Schedulers;
import hu.akarnokd.xml.XElement;

public class RxHttpTest {
    
    Function<Integer, XElement> marshaller = v -> new XElement("integer", v);
    Function<XElement, Integer> unmarshaller = v -> Integer.parseInt(v.content);
    
    @Test
    public void testRemoteRange() throws Exception {
        
        try (RxHttpServer server = new RxHttpServer(8998, Schedulers.io())) {
            
            Map<String, Function<Observable<XElement>, Observable<XElement>>> map = new HashMap<>();
            
            map.put("range", o -> Observable.range(1, 100000).map(marshaller));
            
            server.addEndpoint("/range", map, v -> v);
            
            server.start();

            RxHttpClient client = new RxHttpClient(new URL("http://localhost:8998/range"));
            
            Observable<Integer> retrieve = client.retrieve("range", unmarshaller);
            
            retrieve.count().subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
        }
    }

    @Test
    public void testRemoteRangeBackpressure() throws Exception {
        
        try (RxHttpServer server = new RxHttpServer(8998, Schedulers.io())) {
            
            Map<String, Function<Observable<XElement>, Observable<XElement>>> map = new HashMap<>();
            
            map.put("range", o -> Observable.range(1, 100000).map(marshaller));
            
            server.addEndpoint("/range", map, v -> v);
            
            server.start();

            RxHttpClient client = new RxHttpClient(new URL("http://localhost:8998/range"));
            
            Observable<Integer> retrieve = client.retrieve("range", unmarshaller);
            
            retrieve
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .take(10)
            .toBlocking()
            .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
        }
    }

    @Test
    public void testRemoteSend() throws Exception {
        try (RxHttpServer server = new RxHttpServer(8998, Schedulers.io())) {
            Map<String, Function<Observable<XElement>, Observable<XElement>>> map = new HashMap<>();
            
            map.put("send", o -> o.doOnNext(System.out::println).ignoreElements());
            
            server.addEndpoint("/range", map, v -> v);
            
            server.start();
            
            RxHttpClient client = new RxHttpClient(new URL("http://localhost:8998/range"));
            
            Observable<Void> send = client.send("send", Observable.range(1, 10), marshaller);
            
            send
            .subscribeOn(Schedulers.io())
            .toBlocking().subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

        }        
    }
    
    @Test
    public void testRemoteMap() throws Exception {
        try (RxHttpServer server = new RxHttpServer(8998, Schedulers.io())) {
            Map<String, Function<Observable<XElement>, Observable<XElement>>> map = new HashMap<>();
            
            map.put("map", o -> o.map(unmarshaller).map(v -> v * 2).map(marshaller));
            
            server.addEndpoint("/range", map, v -> v);
            
            server.start();
            
            RxHttpClient client = new RxHttpClient(new URL("http://localhost:8998/range"));
            
            Observable<Integer> send = client.map("map", Observable.range(1, 10), marshaller, unmarshaller);
            
            send
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .toBlocking()
            .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

        }        
    }
    
}
