package hu.akarnokd.reactiverpc;

import java.net.InetAddress;
import java.util.Objects;
import java.util.function.Consumer;

import hu.akarnokd.rxjava2.disposables.Disposable;

public final class RpcClient<T> {

    private RpcClient(Class<T> remoteAPI, Object localAPI) {
    }
    
    public static RpcClient<Void> createLocal(Object localAPI) {
        Objects.requireNonNull(localAPI, "localAPI");
        return new RpcClient<>(null, localAPI);
    }
    
    public static <T> RpcClient<T> createRemote(Class<T> remoteAPI) {
        Objects.requireNonNull(remoteAPI, "remoteAPI");
        return new RpcClient<>(remoteAPI, null);
    }
    
    public static <T> RpcClient<T> createBidirectional(Class<T> remoteAPI, Object localAPI) {
        Objects.requireNonNull(remoteAPI, "remoteAPI");
        Objects.requireNonNull(localAPI, "localAPI");
        return new RpcClient<>(remoteAPI, localAPI);
    }
    
    public T connect(InetAddress endpoint, int port, Consumer<Disposable> close) {
        return null;
    }
}
