package hu.akarnokd.reactor;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import rsc.publisher.Px;

public class ZipTest {
    public static void main(String[] args) {
        Publisher<Integer> source = Px.just(1).hide();
        Flux.zip(source, source, source).doOnNext(v -> System.out.println(v.getT1() + ", " + v.getT2() + ", " + v.getT3())).subscribe();
    }
}
