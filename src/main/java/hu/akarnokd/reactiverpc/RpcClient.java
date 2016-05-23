package hu.akarnokd.reactiverpc;

import java.net.InetAddress;
import java.util.Objects;

public class RpcClient<T extends AutoCloseable> {

    private RpcClient(Class<T> remoteAPI, Object localAPI) {
    }
    
    public static RpcClient<AutoCloseable> createLocal(Object localAPI) {
        Objects.requireNonNull(localAPI, "localAPI");
        return new RpcClient<>(null, localAPI);
    }
    
    public static <T extends AutoCloseable> RpcClient<T> createRemote(Class<T> remoteAPI) {
        Objects.requireNonNull(remoteAPI, "remoteAPI");
        return new RpcClient<>(remoteAPI, null);
    }
    
    public static <T extends AutoCloseable> RpcClient<T> createBidirectional(Class<T> remoteAPI, Object localAPI) {
        Objects.requireNonNull(remoteAPI, "remoteAPI");
        Objects.requireNonNull(localAPI, "localAPI");
        return new RpcClient<>(remoteAPI, localAPI);
    }
    
    public T connect(InetAddress endpoint, int port) {
        return null;
    }
}
