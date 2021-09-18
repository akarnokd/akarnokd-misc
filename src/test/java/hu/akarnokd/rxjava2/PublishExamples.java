package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;

public class PublishExamples {

    @Test
    public void test1() {
        Observable<Object> mixedSource = Observable.<Object>just("a", 1, "b", 2, "c", 3)
                .doOnSubscribe(s -> System.out.println("Subscribed!"));


          mixedSource.compose(f ->
             Observable.merge(
                f.ofType(Integer.class).compose(g -> g.map(v -> v + 1)),
                f.ofType(String.class).compose(g -> g.map(v -> v.toUpperCase()))
             )
          )
          .subscribe(System.out::println);
    }

    @Test
    public void test2() {
        Observable<Object> mixedSource = Observable.<Object>just("a", 1, "b", 2, "c", 3)
                .doOnSubscribe(s -> System.out.println("Subscribed!"));


          mixedSource.publish(f ->
             Observable.merge(
                f.ofType(Integer.class).compose(g -> g.map(v -> v + 1)),
                f.ofType(String.class).compose(g -> g.map(v -> v.toUpperCase()))
             )
          )
          .subscribe(System.out::println);
    }
}
