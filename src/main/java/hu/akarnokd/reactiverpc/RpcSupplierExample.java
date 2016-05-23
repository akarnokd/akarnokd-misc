package hu.akarnokd.reactiverpc;

import org.reactivestreams.Publisher;

public interface RpcSupplierExample {
    
    // client.send
    void receive(RpcStreamContext<Void> ctx, Publisher<Integer> input);
    
    // client.receive
    Publisher<Integer> send(RpcStreamContext<Void> ctx);
    
    // client.map & client.umap
    Publisher<Integer> umap(RpcStreamContext<Void> ctx, Publisher<Integer> input);
}
