package hu.akarnokd.reactiverpc;

import java.util.function.Function;

import org.reactivestreams.Publisher;

public interface RpcConsumerExample {

    void send(Publisher<Integer> data);
    
    Publisher<Integer> receive();
    
    Publisher<Integer> map(Publisher<Integer> data);
    
    void umap(Function<Publisher<Integer>, Publisher<Integer>> mapper);
}
