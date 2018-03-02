package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class FileReaderTest {

    @Test
    public void fileReader() throws Exception {
        Flowable.<Iterator<String>>fromCallable(() -> Files.lines(Paths.get("files/ospd.txt")).iterator())
        .flatMapIterable(v -> () -> v)
        .subscribeOn(Schedulers.io(), false)
        .observeOn(Schedulers.io())
        .doOnNext(v -> System.out.println(Thread.currentThread().getName() + " xxx"))
        .subscribe(System.out::println);
        
        Thread.sleep(10000);
    }
    

    @Test
    public void fileReader2() throws Exception {
        Flowable.<String>create(emitter -> { 
            try (Stream<String> s = Files.lines(Paths.get("files/ospd.txt"))) {
                s.limit(30).forEach(emitter::onNext);
                emitter.onComplete();
            } catch (IOException ex) {
                emitter.onError(ex);
            }
        }, BackpressureStrategy.BUFFER)
        .observeOn(Schedulers.io(), false, 8)
        .subscribeOn(Schedulers.io(), false)
        .doOnNext(v -> System.out.println(Thread.currentThread().getName() + " xxx"))
        .subscribe(System.out::println);
        
        Thread.sleep(10000);
    }
    
}
