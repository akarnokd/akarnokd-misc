package hu.akarnokd.rxjava;

import rx.Observable;

public class GroupByTest {
    public static void main(String[] args) {
    Observable<String> names = Observable.just(
            "John", "Steve", "Ruth",
            "Sam", "Jane", "James");
    
    names.groupBy(s -> s.charAt(0))
    .flatMap(grp -> grp.publish(o -> o.first().concatWith(o.ignoreElements())))
    .subscribe(s -> System.out.println(s));
    }
}
