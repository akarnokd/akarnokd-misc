package hu.akarnokd.reactor;

import org.reactivestreams.Publisher;

import reactivestreams.commons.publisher.PublisherBase;
import reactor.core.publisher.Flux;

public class ZipTest {
    public static void main(String[] args) {
        Publisher<Integer> source = PublisherBase.just(1).hide();
        Flux.zip(source, source, source).doOnNext(v -> System.out.println(v.t1 + ", " + v.t2 + ", " + v.t3)).subscribe();
    }
}
