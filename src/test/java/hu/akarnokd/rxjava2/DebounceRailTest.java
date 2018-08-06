package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.PublishSubject;

public class DebounceRailTest {

    @Test
    public void test() {
        PublishSubject<String> subject = PublishSubject.create();
        
        TestScheduler sch = new TestScheduler();
        
        subject.compose(debounceOnly(v -> v.startsWith("A"), 100, TimeUnit.MILLISECONDS, sch))
        .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
        
        subject.onNext("A1");
        
        sch.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        
        subject.onNext("B1");
        sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);

        subject.onNext("C1");
        sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);

        subject.onNext("A2");
        sch.advanceTimeBy(50, TimeUnit.MILLISECONDS);

        subject.onNext("A3");
        sch.advanceTimeBy(100, TimeUnit.MILLISECONDS);

        subject.onNext("A4");
        sch.advanceTimeBy(50, TimeUnit.MILLISECONDS);

        subject.onNext("B2");
        sch.advanceTimeBy(100, TimeUnit.MILLISECONDS);

        subject.onNext("C2");
        sch.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        
        subject.onComplete();
    }
    
    public static <T> ObservableTransformer<T, T> debounceOnly(Predicate<? super T> condition, long time, TimeUnit unit, Scheduler scheduler) {
        return o -> o.publish(f ->
            f.concatMapEager(v -> {
                if (condition.test(v)) {
                    return Observable.just(v).delay(time, unit, scheduler).takeUntil(f);
                }
                return Observable.just(v);
            })
        );
    }
}
