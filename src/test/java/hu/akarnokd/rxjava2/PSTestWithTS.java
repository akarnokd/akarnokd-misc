package hu.akarnokd.rxjava2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.PublishSubject;

public class PSTestWithTS {

    PublishSubject<String> source = PublishSubject.create();

    String value = "initial";

    void prepare(Scheduler sub, Scheduler on) {
        source
        .subscribeOn(sub)
        .observeOn(on)
        .subscribe(v -> value = v);
    }

    @Test
    public void test() {
        String testString = "testString";
        TestScheduler sch = new TestScheduler();

        prepare(sch, sch);

        sch.triggerActions();

        source.onNext(testString);

        sch.triggerActions();

        assertEquals(testString, value);
    }
}
