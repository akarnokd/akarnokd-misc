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

package hu.akarnokd.io;

import java.io.*;

public class DebugOutputStream extends OutputStream {

    final OutputStream actual;
    
    public DebugOutputStream(OutputStream actual) {
        this.actual = actual;
    }
    
    @Override
    public void write(int b) throws IOException {
        System.out.write(b);
        actual.write(b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        System.out.write(b, off, len);
        actual.write(b, off, len);
    }

}
