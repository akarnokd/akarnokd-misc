package hu.akarnokd.rxjava2;
import org.reactivestreams.Subscription;

import io.reactivex.*;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class RxJavaFuseTest {
    public static void main(String[] args) throws Exception {
        FlowableSubscriber<Object> sequentialSubscriber = new FlowableSubscriber<Object>() {
            private Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                this.s.request(1);
            }

            @Override
            public void onNext(Object x) {
                s.request(1);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                System.exit(0);
            }

            @Override
            public void onComplete() {
                System.out.println("Completed");
                System.exit(0);
            }
        };

        Flowable.range(0, 10)
                .observeOn(RxJavaPlugins.createSingleScheduler(r -> new Thread(r, "producer")), false, 1)
                //.subscribeOn(RxJavaPlugins.createSingleScheduler(r -> new Thread(r, "producer")))
                .doOnNext(aLong -> {
                    System.out.println(aLong + " emitting on " + Thread.currentThread().getName());
                })
                .hide()
                .parallel(2, 1)
                .runOn(Schedulers.computation(), 1)
                .doOnNext(aLong -> System.out.println(aLong + " processing on " + Thread.currentThread().getName()))
                .sequential()
                .subscribe(sequentialSubscriber);
        
        Thread.sleep(5000);
    }
}