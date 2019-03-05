package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.TestScheduler;

public class WindowBoundaryIssue {

    @Test
    public void test() {
PublishProcessor<Flowable<String>> flush = PublishProcessor.create();
PublishProcessor<String> restProcessor = PublishProcessor.create();

int count = 2;
int delay = 100;

TestScheduler scheduler = new TestScheduler();

Flowable<Flowable<String>> boundary = restProcessor.window(
        delay, TimeUnit.MILLISECONDS, scheduler, count, true)
                .mergeWith(flush);

PublishProcessor<Flowable<String>> tempBoundary = PublishProcessor.create();

restProcessor
                .window(tempBoundary)
                .concatMapSingle(Flowable::toList)
                .filter(it -> !it.isEmpty())
                .subscribe(strings -> System.out.println("emit REST call >>> " + strings));

boundary.subscribe(tempBoundary);

restProcessor.offer("1");
restProcessor.offer("2");

restProcessor.offer("3");
restProcessor.offer("4");

restProcessor.offer("5");
restProcessor.offer("6");

restProcessor.offer("7");
restProcessor.offer("8");

restProcessor.offer("9");
flush.offer(Flowable.just("flush"));

restProcessor.offer("10");

System.out.println("onComplete");

restProcessor.onComplete();
flush.onComplete();
    }
}
