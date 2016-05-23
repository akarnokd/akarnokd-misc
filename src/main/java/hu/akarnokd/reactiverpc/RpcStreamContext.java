package hu.akarnokd.reactiverpc;

import java.net.InetAddress;

public interface RpcStreamContext<T> {

    InetAddress clientAddress();
    
    int clientPort();
    
    void set(String attribute, Object o);
    
    <U> U get(String attribute);

    <U> U get(String attribute, U defaultValue);

    void remove(String attribute);
    
    boolean has(String attribute);
    
    T remoteAPI();
}
