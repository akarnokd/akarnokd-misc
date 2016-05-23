package hu.akarnokd.reactiverpc;

import java.net.InetAddress;
import java.util.Objects;

import rx.Scheduler;
import rx.schedulers.Schedulers;

public final class RpcServer<T> {
    
    static final Scheduler ACCEPTOR = Schedulers.io();
    
    private RpcServer(Object localAPI, Class<T> remoteAPI) {
        
    }
    
    public static RpcServer<Void> createLocal(Object localAPI) {
        Objects.requireNonNull(localAPI, "localAPI");
        return new RpcServer<>(localAPI, null);
    }
    
    public static <T> RpcServer<T> createRemote(Class<T> remoteAPI) {
        Objects.requireNonNull(remoteAPI, "remoteAPI");
        return new RpcServer<>(null, remoteAPI);
    }
    
    public static <T> RpcServer<T> createBidirectional(Object localAPI, Class<T> remoteAPI) {
        Objects.requireNonNull(localAPI, "localAPI");
        Objects.requireNonNull(remoteAPI, "remoteAPI");
        return new RpcServer<>(localAPI, remoteAPI);
    }
    
    public AutoCloseable start(int port) {
        return null;
    }
    
    public AutoCloseable start(InetAddress localAddress, int port) {
        return null;
    }

    public AutoCloseable start(int port, Scheduler dispatcher) {
        return null;
    }
    
    public AutoCloseable start(InetAddress localAddress, int port, Scheduler dispatcher) {
        return null;
    }

}
