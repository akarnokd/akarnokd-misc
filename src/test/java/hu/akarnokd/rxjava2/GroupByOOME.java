package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.*;

public class GroupByOOME {
    @Test
    public void test() {
        int n = 10_000;
        Flowable.range(2, 2 * n)
        .groupBy(e -> {
            // First 100 000 000 in different group
            if (e < n) {
                return e;
            }
            if (e.intValue() % (n / 100) == 0) {
                System.out.println("Grouping: " + e);
            }
            // Last 100 000 000 in the same group
            return 1;
        })
        .subscribe(g -> {
            if (g.getKey() == 1) {
                g.subscribe(TestSubscriber.create(0));
            } else {
                g.subscribe(new DefaultSubscriber<Integer>() {
                    @Override
                    public void onNext(Integer t) {
                        if (t.intValue() % 100_000 == 0) {
                            System.out.println("Received: " + t);
                        }
                        Schedulers.computation().scheduleDirect(this::cancel);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
            }
        });
    }
}
