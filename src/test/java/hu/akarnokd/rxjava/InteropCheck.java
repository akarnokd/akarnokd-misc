package hu.akarnokd.rxjava;

import org.junit.Test;

import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class InteropCheck {

    @Test
    public void test() {
        rx.subjects.PublishSubject<String> sj = rx.subjects.PublishSubject.create();
        io.reactivex.subjects.Subject<String> sj2 = RxJavaInterop.toV2Subject(sj);
    }
}
