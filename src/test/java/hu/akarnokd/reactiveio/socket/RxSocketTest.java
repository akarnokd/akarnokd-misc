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

import org.junit.Test;

import hu.akarnokd.rxjava2.Observable;
import hu.akarnokd.rxjava2.functions.Function;
import hu.akarnokd.rxjava2.schedulers.Schedulers;
import hu.akarnokd.xml.XElement;

public class RxSocketTest {
    @Test
    public void testRemoteRange() throws Exception {
        
        try (RxServerSocket server = new RxServerSocket()) {
            server.bind(8989);
            
            RxSocketDispatcher sd = new RxSocketDispatcher(o -> Observable.range(1, 100000)
                    .map(v -> new XElement("integer", v)));
            
            server.accept().subscribe(sd);
            
            RxClientSocket client = new RxClientSocket("localhost", 8989);
            
            Observable<Integer> retrieve = client.retrieve(x -> Integer.parseInt(x.content));
            
            retrieve.count().subscribe(System.out::println, Throwable::printStackTrace);
        }
    }

    @Test
    public void testRemoteRangeAsync() throws Exception {
        
        try (RxServerSocket server = new RxServerSocket()) {
            server.bind(8989);
            
            RxSocketDispatcher sd = new RxSocketDispatcher(o -> Observable.range(1, 100000)
                    .map(v -> new XElement("integer", v)));
            
            server.accept().subscribe(sd);
            
            RxClientSocket client = new RxClientSocket("localhost", 8989);
            
            Observable<Integer> retrieve = client.retrieve(x -> Integer.parseInt(x.content));
            
            retrieve
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .take(10)
            .toBlocking()
            .subscribe(System.out::println, Throwable::printStackTrace);
        }
    }

    @Test
    public void testSendRemote() throws Exception {
        
        try (RxServerSocket server = new RxServerSocket()) {
            server.bind(8989);
            
            RxSocketDispatcher sd = new RxSocketDispatcher(o -> {
                return o.doOnNext(System.out::println).ignoreElements();
            });
            
            server.accept().subscribe(sd);
            
            RxClientSocket client = new RxClientSocket("localhost", 8989);
            
            Observable<Void> retrieve = client.send(Observable.range(1, 10), v -> new XElement("integer", v));
            
            retrieve
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .take(10)
            .toBlocking()
            .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
        }
    }

    @Test
    public void testMapRemote() throws Exception {
        
        try (RxServerSocket server = new RxServerSocket()) {
            server.bind(8989);

            Function<XElement, Integer> unmarshall = v -> Integer.parseInt(v.content);
            Function<Integer, XElement> marshall = v -> new XElement("integer", v);

            RxSocketDispatcher sd = new RxSocketDispatcher(o -> {
                return o
                        .map(unmarshall).map(v -> v * 2).map(marshall);
            });
            
            server.accept().subscribe(sd);
            
            RxClientSocket client = new RxClientSocket("localhost", 8989);
            
            Observable<Integer> retrieve = client.map(Observable.range(1, 10), marshall, unmarshall);
            
            retrieve
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .take(10)
            .toBlocking()
            .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
        }
    }

}