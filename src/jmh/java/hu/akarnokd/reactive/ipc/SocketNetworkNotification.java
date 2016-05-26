package hu.akarnokd.reactive.ipc;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh='IxPerf'
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SocketNetworkNotification {
    
    OutputStream out;
    
    ExecutorService exec;

    ServerSocket server;

    Socket client;
    
    CyclicBarrier awaitConnection;
    CyclicBarrier awaitClient;
    CyclicBarrier awaitComplete;
    
    @Setup(Level.Iteration)
    public void setup() throws Exception {
        exec = Executors.newCachedThreadPool();
        awaitConnection = new CyclicBarrier(2);
        awaitClient = new CyclicBarrier(2);
        awaitComplete = new CyclicBarrier(2);
        
        server = new ServerSocket(23345);
        exec.execute(this::serverLoop);
        
        client = new Socket(InetAddress.getLocalHost(), 23345);
        out = client.getOutputStream();
        
        awaitConnection.await();
    }
    
    void serverLoop() {
        try {
            Socket ssocket = server.accept();
            
            InputStream in = ssocket.getInputStream();
            
            awaitConnection.await();
            
            awaitClient.await();

            while (in.read() != -1) ;
            
            awaitComplete.await();
        } catch (Throwable ex) {
        }
    }
    
    @TearDown(Level.Iteration)
    public void teardown() throws Exception {
        exec.shutdown();
        server.close();
        client.close();
    }

    @Benchmark
    public void send() throws Exception {
        awaitClient.await();
        
        for (int i = 0; i < 1_000_000; i++) {
            out.write(1);
            out.flush();
        }
        out.close();
        
        awaitComplete.await();
    }
}
