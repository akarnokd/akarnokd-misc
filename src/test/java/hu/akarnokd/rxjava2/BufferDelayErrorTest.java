package hu.akarnokd.rxjava2;

import java.util.List;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.processors.PublishProcessor;

public class BufferDelayErrorTest {


    @Test
    public void test() {
        Flowable.range(0, 10)
        .doOnNext(x -> {
            if(x == 7) {
                throw new RuntimeException();
            }
        })
        .compose(bufferDelayError(5))
        .subscribe(System.out::println, System.out::println);
    }

    static <T> FlowableTransformer<T, List<T>> bufferDelayError(int size) {
        return o -> Flowable.defer(() -> {
            PublishProcessor<List<T>> ps = PublishProcessor.create();

            return o
                .doOnComplete(() -> ps.onComplete())
                .onErrorResumeNext(e -> {
                     ps.onError(e);
                     return Flowable.empty();
                })
                .buffer(size)
                .concatWith(ps);
        });
     }
}
