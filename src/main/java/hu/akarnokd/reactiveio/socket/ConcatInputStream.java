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

public final class ConcatInputStream extends InputStream {
    final InputStream[] streams;
    int index;
    public ConcatInputStream(InputStream... streams) {
        this.streams = streams;
    }
    
    @Override
    public int read() throws IOException {
        for (;;) {
            if (index == streams.length) {
                return -1;
            }
            InputStream in = streams[index];
            int r = in.read();
            if (r >= 0) {
                return r;
            }
            index++;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        for (;;) {
            if (index == streams.length) {
                return -1;
            }
            
            InputStream in = streams[index];
            
            int r = in.read(b, off, len);
            
            if (r >= 0) {
                return r;
            }
            
            index++;
        }
    }
    
    @Override
    public int available() throws IOException {
        if (index == streams.length) {
            return 0;
        }
        
        InputStream in = streams[index];
        return in.available();
    }
    
    @Override
    public void close() throws IOException {
        for (InputStream in : streams) {
            in.close();
        }
    }
}
