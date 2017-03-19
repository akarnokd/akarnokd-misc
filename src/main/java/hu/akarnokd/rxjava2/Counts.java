package hu.akarnokd.rxjava2;

import io.reactivex.Flowable;
import reactor.core.publisher.Flux;

public final class Counts {

    private Counts() { }

    public static void main(String[] args) {
        Flowable.fromArray(Flowable.class.getMethods())
        .filter(m -> m.getDeclaringClass() == Flowable.class)
        .map(m -> m.getName())
        .distinct()
        .count()
        .subscribe(System.out::println);

        Flowable.fromArray(Flux.class.getMethods())
        .filter(m -> m.getDeclaringClass() == Flux.class)
        .map(m -> m.getName())
        .distinct()
        .count()
        .subscribe(System.out::println);
}
}
