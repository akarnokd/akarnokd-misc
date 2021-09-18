package hu.akarnokd.rxjava;

    import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx.Scheduler.Worker;
import rx.exceptions.MissingBackpressureException;
import rx.observers.AssertableSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.*;

    public class PublishSubjectRaceTest {

        @Test
        public void racy() throws Exception {
            Worker worker = Schedulers.computation().createWorker();
            try {
                for (int i = 0; i < 1000; i++) {
                    AtomicInteger wip = new AtomicInteger(2);

                    PublishSubject<Integer> ps = PublishSubject.create();

                    AssertableSubscriber<Integer> as = ps.test(1);

                    CountDownLatch cdl = new CountDownLatch(1);

                    worker.schedule(() -> {
                        if (wip.decrementAndGet() != 0) {
                            while (wip.get() != 0) ;
                        }
                        ps.onNext(1);

                        cdl.countDown();
                    });
                    if (wip.decrementAndGet() != 0) {
                        while (wip.get() != 0) ;
                    }
                    ps.onNext(1);

                    cdl.await();

                    as.assertFailure(MissingBackpressureException.class, 1);
                }
            } finally {
                worker.unsubscribe();
            }
        }

        @Test
        public void nonRacy() throws Exception {
            Worker worker = Schedulers.computation().createWorker();
            try {
                for (int i = 0; i < 1000; i++) {
                    AtomicInteger wip = new AtomicInteger(2);

                    Subject<Integer, Integer> ps = PublishSubject.<Integer>create().toSerialized();

                    AssertableSubscriber<Integer> as = ps.test(1);

                    CountDownLatch cdl = new CountDownLatch(1);

                    worker.schedule(() -> {
                        if (wip.decrementAndGet() != 0) {
                            while (wip.get() != 0) ;
                        }
                        ps.onNext(1);

                        cdl.countDown();
                    });
                    if (wip.decrementAndGet() != 0) {
                        while (wip.get() != 0) ;
                    }
                    ps.onNext(1);

                    cdl.await();

                    as.assertFailure(MissingBackpressureException.class, 1);
                }
            } finally {
                worker.unsubscribe();
            }
        }
    }
