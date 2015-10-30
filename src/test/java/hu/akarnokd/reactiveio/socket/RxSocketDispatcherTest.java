package hu.akarnokd.reactiveio.socket;

import java.io.*;

import org.junit.Test;

import hu.akarnokd.reactiveio.socket.RxSocketDispatcher.XElementWriterObserver;
import hu.akarnokd.rxjava2.subjects.UnicastSubject;

public class RxSocketDispatcherTest {
    @Test
    public void singleEmptyRequestParsing() throws Exception {
        
        InputStream in = new ByteArrayInputStream("<request n='1'/>".getBytes());
        
        OutputStream out = new ByteArrayOutputStream();
        
        XElementWriterObserver observer = new XElementWriterObserver(out);
        
        RxSocketDispatcher.parseXMLRequest(in, out, observer , UnicastSubject.create());
        
    }
    
    @Test
    public void manyEmptyRequestParsing() throws Exception {
        
        InputStream in = new ByteArrayInputStream("<request n='1'/><request n='1'/><request n='1'/>".getBytes());
        
        OutputStream out = new ByteArrayOutputStream();
        
        XElementWriterObserver observer = new XElementWriterObserver(out);
        
        RxSocketDispatcher.parseXMLRequest(in, out, observer , UnicastSubject.create());
        
    }
}
