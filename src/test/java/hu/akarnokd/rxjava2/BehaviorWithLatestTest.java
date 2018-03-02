package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;

public class BehaviorWithLatestTest {

    @Test
    public void bothInit() {
        BehaviorSubject<String> s1 = BehaviorSubject.createDefault("");
        BehaviorSubject<String> s2 = BehaviorSubject.createDefault("");
        
        s1.withLatestFrom(s2, (a, b) -> true)
        .test()
        .assertValue(true);
    }

    @Test
    public void firstInit() {
        BehaviorSubject<String> s1 = BehaviorSubject.createDefault("");
        BehaviorSubject<String> s2 = BehaviorSubject.create();

        TestObserver<Boolean> to = s1.withLatestFrom(s2, (a, b) -> true)
        .test();

        to.assertEmpty();

        s2.onNext("");

        to.assertEmpty();

        s1.onNext("");

        to.assertValue(true);
    }

    @Test
    public void secondInit() {
        BehaviorSubject<String> s1 = BehaviorSubject.create();
        BehaviorSubject<String> s2 = BehaviorSubject.createDefault("");

        TestObserver<Boolean> to = s1.withLatestFrom(s2, (a, b) -> true)
        .test();

        to.assertEmpty();

        s1.onNext("");

        to.assertValue(true);
    }
}
