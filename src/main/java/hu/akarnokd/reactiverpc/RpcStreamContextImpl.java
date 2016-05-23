package hu.akarnokd.reactiverpc;

import java.net.InetAddress;
import java.util.concurrent.*;

public final class RpcStreamContextImpl<T> implements RpcStreamContext<T> {
    
    final InetAddress address;
    
    final int port;
    
    final ConcurrentMap<String, Object> map;
    
    final T remoteAPI;
    
    public RpcStreamContextImpl(InetAddress address, int port, T remoteAPI) {
        this.address = address;
        this.port = port;
        this.remoteAPI = remoteAPI;
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public InetAddress clientAddress() {
        return address;
    }

    @Override
    public int clientPort() {
        return port;
    }

    @Override
    public void set(String attribute, Object o) {
        map.put(attribute, o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U get(String attribute) {
        return (U)map.get(attribute);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U get(String attribute, U defaultValue) {
        return (U)map.getOrDefault(attribute, defaultValue);
    }

    @Override
    public void remove(String attribute) {
        map.remove(attribute);
    }

    @Override
    public boolean has(String attribute) {
        return map.containsKey(attribute);
    }

    @Override
    public T remoteAPI() {
        return remoteAPI;
    }
}
