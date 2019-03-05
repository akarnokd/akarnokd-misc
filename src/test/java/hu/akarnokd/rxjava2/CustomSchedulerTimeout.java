package hu.akarnokd.rxjava2;

import java.util.Iterator;
import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class CustomSchedulerTimeout {

    @Test
    public void test() throws Exception {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
        Scheduler schedulerFromExecutor = Schedulers.from(scheduledExecutorService);

        Flowable<Integer> results = Flowable.fromIterable(()-> {

            return new Iterator<Integer>() {
                @Override
                public boolean hasNext()
                {
                    try
                    {
                        Thread.sleep(30000);
                        return false;
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Interrupted! " + e);
                        return true;
                    }
                }

                @Override
                public Integer next()
                {
                    return 2;
                }
            };

        }).subscribeOn(schedulerFromExecutor);//change to Schedulers.io() to make it work.

        results.timeout(1000, TimeUnit.MILLISECONDS, Schedulers.single(), Flowable.error(new TimeoutException("Timed out")))
                .doOnTerminate(()-> System.out.println("Finished"))
                .subscribe(r-> System.out.println("Got " + r), e-> System.out.println("Error " + e));

        Thread.sleep(200000);
    }
}
