package hu.akarnokd.rxjava;

import rx.Observable;
import rx.functions.Func1;

public class Ternary {
    public static void main(String[] args) {
        Func1<String, Observable<String>> func = s ->  s.contains("1") ?  Observable.<String>just(s) :  Observable.empty();  
              Observable.<String>just("A1", "A2")
            .flatMap( s ->  (s.contains("1")) ? Observable.<String>just(s) : Observable.<String>empty() )//   <--------- this approach did not work
              .flatMap( s -> { return (s.contains("1")) ? Observable.<String>just(s) : Observable.<String>empty(); } )//   <--------- this approach did not work
              .flatMap(func) // <---- this worked
              .subscribe(s -> {System.out.println(s);});
    }
}
