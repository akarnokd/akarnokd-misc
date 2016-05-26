package hu.akarnokd.reactive.ipc;

import java.io.*;
import java.net.Socket;

import rsc.scheduler.Scheduler;
import rsc.scheduler.Scheduler.Worker;

public final class IpcEndpoint implements Closeable {

    IpcFileInterop input;
    
    IpcFileInterop output;
    
    IpcFileInterop wipIn;
    
    IpcFileInterop wipOut;
    
    InputStream in;
    
    OutputStream out;
    
    Worker dataReader;
    
    Worker dataWriter;
    
    Worker signalReader;
    
    Worker signalWriter;
    
    byte[] readBuffer;
    
    byte[] writeBuffer;
    
    public IpcEndpoint(Scheduler scheduler, Socket socket, String fileNameBase, int maxSize) throws IOException {
        
        in = socket.getInputStream();
        
        out = socket.getOutputStream();
        
        dataReader = scheduler.createWorker();
        
        dataWriter = scheduler.createWorker();
        
        signalReader = scheduler.createWorker();
        
        signalWriter = scheduler.createWorker();
        
        wipIn = new IpcFileInterop(fileNameBase + "-wipin.dat", 4);

        wipOut = new IpcFileInterop(fileNameBase + "-wipout.dat", 4);

        input = new IpcFileInterop(fileNameBase + "-in.dat", 16 * 1024);

        output = new IpcFileInterop(fileNameBase + "-in.dat", 16 * 1024);
        
        readBuffer = new byte[1];
        
        writeBuffer = new byte[1];
    }
    
    void readLoop() {
        
    }
    
    @Override
    public void close() throws IOException {
        
        dataReader.shutdown();
        
        dataWriter.shutdown();
        
        signalReader.shutdown();
        
        signalWriter.shutdown();
        
        wipIn.close();
        
        wipOut.close();
        
        input.close();
        
        output.close();
    }
}
