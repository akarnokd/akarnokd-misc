package hu.akarnokd.reactiverpc;

import org.reactivestreams.Publisher;

public interface RpcSupplierExample {
    
    // client.send
    void receive(RpcStreamContext ctx, Publisher<Integer> input);
    
    // client.receive
    Publisher<Integer> send(RpcStreamContext ctx);
    
    // client.map & client.umap
    Publisher<Integer> umap(RpcStreamContext ctx, Publisher<Integer> input);
}
