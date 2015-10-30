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
            
            Iterable<Integer> blocking = retrieve
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .take(10)
            .toBlocking();
            
            blocking
            .forEach(System.out::println);
        }
    }

}