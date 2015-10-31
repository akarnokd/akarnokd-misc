package hu.akarnokd.reactiveio.socket;

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
