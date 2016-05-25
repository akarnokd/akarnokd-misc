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

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import hu.akarnokd.reactive.io.RxIODispatcher;
import hu.akarnokd.rxjava2.*;
import hu.akarnokd.rxjava2.functions.Function;
import hu.akarnokd.xml.XElement;

public class RxHttpServer implements Closeable {
    HttpServer server;
    public RxHttpServer(int port, Scheduler scheduler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(r -> {
            scheduler.scheduleDirect(r);
        });
    }
    

    public void addEndpoint(String path, 
            Map<String, Function<Observable<XElement>, Observable<XElement>>> functions,
            Function<Observable<XElement>, Observable<XElement>> defaultFunction) {
        server.createContext(path, exch -> {
            exch.sendResponseHeaders(200, 0);
            RxIODispatcher.dispatch(exch.getRequestBody(), exch.getResponseBody(), functions::get, defaultFunction);
        });
    }
    
    
    public void start() {
        server.start();
    }
    
    @Override
    public void close() {
        server.stop(2);
    }
}
