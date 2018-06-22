package hu.akarnokd.rxjava2;

import org.junit.Test;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.*;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SomeClicks {

    @Test
    public void test() throws Exception {
        BlockingScheduler blocking = new BlockingScheduler();
        blocking.execute(() -> {
            Flowable
            .create(new FlowableOnSubscribe<Integer>() {
                @Override
                public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                    for (int i = 0; i < 1000; i++) {
                        emitter.onNext(i);
                    }
                }
            }, BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.newThread(), false)
            .observeOn(blocking)
            .subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    System.out.println("LATEST " + integer + "");
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    System.out.println("LATEST Throwable: " + throwable.getMessage());
                }
            }, () -> blocking.shutdown());
        });
    }
}
