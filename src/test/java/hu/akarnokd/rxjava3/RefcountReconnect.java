package hu.akarnokd.rxjava3;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class RefcountReconnect {

    public static void main(String[] args) {
PublishSubject<Integer> ps = PublishSubject.create();

var o1 = Observable.<Integer>defer(() -> {
    System.out.println("Subscribed");
    return ps;
})
.doOnDispose(() -> System.out.println("Unsubscribed"))
.replay(1).refCount();

o1.subscribe().dispose();
System.out.println(ps.hasObservers());
o1.subscribe().dispose();
    }
}
