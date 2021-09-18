package hu.akarnokd.rxjava;

import java.util.concurrent.*;

import rx.*;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class DelayingScheduler {

    public static void main(String[] args) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        final Scheduler sch = Schedulers.from(exec);
        final long delayMillis = 1000;

        Scheduler newScheduler = new Scheduler() {
            @Override public Worker createWorker() {
                return new NewWorker(sch.createWorker());
            }

            class NewWorker extends Scheduler.Worker {
                final Worker actual;

                NewWorker(Worker actual) {
                    this.actual = actual;
                }

                @Override
                public void unsubscribe() {
                    actual.unsubscribe();
                }

                @Override
                public boolean isUnsubscribed() {
                    return actual.isUnsubscribed();
                }

                @Override
                public Subscription schedule(Action0 action) {
                    return actual.schedule(action, delayMillis, TimeUnit.MILLISECONDS);
                }

                @Override
                public Subscription schedule(Action0 action, long delayTime,
                        TimeUnit unit) {
                    return actual.schedule(action, delayTime, unit);
                }
            }
        };

        Observable.just(1)
        .observeOn(newScheduler)
        .toBlocking()
        .subscribe(System.out::println);

        exec.shutdown();
    }
}
