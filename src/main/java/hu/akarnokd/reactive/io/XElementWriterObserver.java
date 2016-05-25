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
import java.util.concurrent.atomic.AtomicBoolean;

import hu.akarnokd.rxjava2.plugins.RxJavaPlugins;
import hu.akarnokd.rxjava2.subscribers.AsyncObserver;
import hu.akarnokd.xml.XElement;

public final class XElementWriterObserver extends AsyncObserver<XElement> {
    final OutputStream out;
    public final AtomicBoolean completing;
    
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