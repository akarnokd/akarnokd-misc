package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.processors.*;

public class ZipInfinite {

    @Test
    public void test() {
        final Flowable<Integer> flo1 = Flowable.just(1).repeat();
        final FlowableProcessor<Integer> flo2 = PublishProcessor.create();
        final Flowable<Integer> result = Flowable.zip(flo2, flo1, (o1, o2) -> {
              return 1;
        }, true, 1);
        result.subscribe(e -> System.out.println(e));
        flo2.onNext(2);
        flo2.onNext(3);
    }

}
