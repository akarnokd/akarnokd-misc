package hu.akarnokd.rxjava;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.TestScheduler;

public class BufferStartEndTest {

    @Test
    public void test() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<String> pp = PublishProcessor.create();

        Function<Flowable<String>, Flowable<List<String>>> f = o -> 
                o.buffer(o.filter(v -> v.contains("Start")), 
                         v -> Flowable.merge(o.filter(w -> w.contains("Start")), 
                                             Flowable.timer(5, TimeUnit.MINUTES, scheduler))); 

        pp.publish(f)
        .doOnNext(v -> {
            int s = v.size();
            if (s > 1 && v.get(s - 1).contains("Start")) {
                v.remove(s - 1);
            }
        })
        .subscribe(System.out::println);

        pp.onNext("Start");
        pp.onNext("A");
        pp.onNext("B");
        pp.onNext("End");

        pp.onNext("Start");
        pp.onNext("C");

        scheduler.advanceTimeBy(5, TimeUnit.MINUTES);

        pp.onNext("Start");
        pp.onNext("D");
        pp.onNext("End");
        pp.onComplete();
    }
}
