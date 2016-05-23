package hu.akarnokd.reactiverpc;

import java.net.InetAddress;

import rx.Scheduler;

public interface RpcStreamContext {
    long streamId();
    
    InetAddress clientAddress();
    
    int clientPort();
    
    void set(String attribute, Object o);
    
    <T> T get(String attribute);

    <T> T get(String attribute, T defaultValue);

    void remove(String attribute);
    
    boolean has(String attribute);
    
    Scheduler scheduler();
}
