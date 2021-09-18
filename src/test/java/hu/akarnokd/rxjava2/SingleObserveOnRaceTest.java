package hu.akarnokd.rxjava2;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import io.reactivex.*;
import io.reactivex.Scheduler.Worker;
import io.reactivex.functions.*;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;

public class SingleObserveOnRaceTest {

    @Test
    public void test() {
        final AtomicBoolean disposed = new AtomicBoolean(false);

        final TestObserver<Long> test =
                Single.timer(1000, TimeUnit.MILLISECONDS, Schedulers.computation())
                .observeOn(Schedulers.single())
                .doOnSuccess(new Consumer<Long>() {
                    @Override
                    public void accept(Long l) throws Exception {
                        if (disposed.get()) {
                            throw new IllegalStateException("Already disposed!");
                        }
                    }
                })
                .test();

        Completable.timer(1000, TimeUnit.MILLISECONDS, Schedulers.single())
        .subscribe(new Action() {
            @Override
            public void run() throws Exception {
                test.dispose();
                disposed.set(true);
            }
        });

        test.awaitDone(3, TimeUnit.SECONDS)
        .assertNoErrors();
    }

    @Test
    public void race() throws Exception {
        Worker w = Schedulers.newThread().createWorker();
        try {
            for (int i = 0; i < 1000; i++) {
                Integer[] value = { 0, 0 };

                TestObserver<Integer> to = new TestObserver<Integer>() {
                    @Override
                    public void onSuccess(Integer v) {
                        value[1] = value[0];
                        super.onSuccess(v);
                    }
                };

                SingleSubject<Integer> subj = SingleSubject.create();

                subj.observeOn(Schedulers.single())
                .onTerminateDetach()
                .subscribe(to);

                AtomicInteger wip = new AtomicInteger(2);
                CountDownLatch cdl = new CountDownLatch(2);

                w.schedule(() -> {
                    if (wip.decrementAndGet() != 0) {
                        while (wip.get() != 0);
                    }
                    subj.onSuccess(1);
                    cdl.countDown();
                });

                Schedulers.single().scheduleDirect(() -> {
                    if (wip.decrementAndGet() != 0) {
                        while (wip.get() != 0);
                    }
                    to.cancel();
                    value[0] = null;
                    cdl.countDown();
                });

                cdl.await();

                Assert.assertNotNull(value[1]);
            }
        } finally {
            w.dispose();
        }
    }
}
