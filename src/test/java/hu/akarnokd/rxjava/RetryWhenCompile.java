package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import rx.*;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RetryWhenCompile {
    static int retryCount = 0;
    public static void main(String[] args) throws Exception {
        
        final int maxRetries = 3;

        Observable.unsafeCreate(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onError(new RuntimeException("always fails"));
            }
        })
        .subscribeOn(Schedulers.immediate())
         .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {

                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                if (++retryCount <= maxRetries) {
                                    // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
                                    System.out.println("get error, it will try after " + 1000 + " millisecond, retry count " + retryCount);
                                    return Observable.timer(1000, TimeUnit.MILLISECONDS);
                                }
                                return Observable.error(throwable);
                            }
                        });
                    }


                })
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onCompleted() {
                        System.out.println("onCompleted");
                    }

                    @Override
                    public void onNext(Integer value) {
                        System.out.println("onSuccess value = " + value);
                    }

                    @Override
                    public void onError(Throwable error) {
                        System.out.println("onError error = " + error);
                    }
                });
        Thread.sleep(10000);
    }
}
