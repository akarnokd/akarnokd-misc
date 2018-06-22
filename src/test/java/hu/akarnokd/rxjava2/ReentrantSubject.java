package hu.akarnokd.rxjava2;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.subjects.*;

public class ReentrantSubject {
    static Subject<Integer> publisher = PublishSubject.<Integer>create().toSerialized();

    @Test
    public void testReactive() throws Exception {
        publisher.buffer(100, TimeUnit.MILLISECONDS, 1)
                .doOnNext(System.out::println)
                .doOnNext(ReentrantSubject::fakeError)
                .subscribe();

        publisher.onNext(0);
        
        Thread.sleep(100000);
    }

    private static void fakeError(List<Integer> integers) {
        integers.forEach(publisher::onNext);
    }
}
