package hu.akarnokd.rxjava;

import rx.*;

public final class SubscriberReuse {
    private SubscriberReuse() { }

    public static void main(String[] args) {
        test1();
        System.out.println("===============================================================");
        test2();
    }
    private static void test1() {
        Observable<String> observable = Observable.unsafeCreate(new Observable.OnSubscribe<String>() {
              @Override
               public void call(Subscriber<? super String> subscriber) {
                    subscriber.onNext("Hello RxJava");
                    subscriber.onCompleted();
               }
             });;
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }
            @Override
            public void onError(Throwable e) {
                System.out.println("onError");
            }
            @Override
            public void onNext(String value) {
                System.out.println("onNext value : " + value);
            }
        };

        observable.subscribe(subscriber);

        System.out.println("----------------");

        observable.subscribe(subscriber);

        System.out.println("*****************");

        Observable.unsafeCreate(new Observable.OnSubscribe<String>() {
              @Override
               public void call(Subscriber<? super String> subscriber) {
                    subscriber.onNext("Second");
                    subscriber.onCompleted();
               }
             }).subscribe(subscriber);
    }
    private static void test2() {
        Observable<String> observable = Observable.just("Hello RxJava");
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }
            @Override
            public void onError(Throwable e) {
                System.out.println("onError");
            }
            @Override
            public void onNext(String value) {
                System.out.println("onNext value : " + value);
            }
        };

        observable.subscribe(subscriber);

        System.out.println("----------------");

        observable.subscribe(subscriber);

        System.out.println("*****************");

        Observable.unsafeCreate(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Second");
                subscriber.onCompleted();
            }
        }).subscribe(subscriber);
    }
}
