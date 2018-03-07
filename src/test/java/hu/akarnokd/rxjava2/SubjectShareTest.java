package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class SubjectShareTest {
    enum Request {
        Request1,
        Request2
    }

    @Test
    public void test() {
        PublishSubject<Request> requestStream = PublishSubject.create();

        Observable<Request> stateChanges = requestStream.share();

        stateChanges
        .delaySubscription(requestStream)
        .subscribe(v -> { System.out.println("received " + v); });

        // Comment this and it changes the output!
        stateChanges.subscribe();

        requestStream.onNext(Request.Request1);
        requestStream.onNext(Request.Request2);
    }
}
