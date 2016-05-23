package hu.akarnokd.reactiverpc;

import java.net.InetAddress;

import rx.Scheduler;

public interface RpcStreamContext<T> {

    long streamId();
    
    InetAddress clientAddress();
    
    int clientPort();
    
    void set(String attribute, Object o);
    
    <U> U get(String attribute);

    <U> U get(String attribute, U defaultValue);

    void remove(String attribute);
    
    boolean has(String attribute);
    
    Scheduler scheduler();
    
    T remoteAPI();
}
