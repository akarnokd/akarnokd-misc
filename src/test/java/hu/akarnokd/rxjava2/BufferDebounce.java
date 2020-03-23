package hu.akarnokd.rxjava2;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.PublishSubject;

public class BufferDebounce {

    @Test
    public void test() {
        PublishSubject<Integer> ps = PublishSubject.create();
        
        TestScheduler sch = new TestScheduler();
        
        ps.compose(bufferDebounce(200, TimeUnit.MILLISECONDS, sch))
        .subscribe(
                v -> System.out.println(sch.now(TimeUnit.MILLISECONDS)+ ": " + v),
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
        
        ps.onNext(1);
        ps.onNext(2);
        
        sch.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        
        ps.onNext(3);

        sch.advanceTimeTo(150, TimeUnit.MILLISECONDS);

        ps.onNext(4);

        sch.advanceTimeTo(400, TimeUnit.MILLISECONDS);

        ps.onNext(5);

        sch.advanceTimeTo(450, TimeUnit.MILLISECONDS);

        ps.onNext(6);

        sch.advanceTimeTo(800, TimeUnit.MILLISECONDS);

        ps.onNext(7);
        ps.onComplete();

        sch.advanceTimeTo(850, TimeUnit.MILLISECONDS);
    }
    
    static <T> ObservableTransformer<T, List<T>> bufferDebounce(long time, TimeUnit unit, Scheduler scheduler) {
        return o ->
            o.publish(v -> 
                v.buffer(v.debounce(time, unit, scheduler)
                    .takeUntil(v.ignoreElements().toObservable())
                )
            );
    }
}
