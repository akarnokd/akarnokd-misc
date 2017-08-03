package hu.akarnokd.rxjava2;

import org.junit.Test;
import org.reactivestreams.*;

import io.reactivex.Flowable;

public class CustomFlowableTest {

    @Test
    public void test() {
        new CustomFlowable().distinctUntilChanged((integer, integer2) -> integer.intValue() == integer2.intValue())
        .subscribe(integer -> System.out.println("Consumer | Value: " + integer.intValue()));
    }
    
    public static final class CustomFlowable extends Flowable<Integer> {

        @Override
        protected void subscribeActual(Subscriber<? super Integer> subscriber) {
            Listener listener = new Listener(subscriber);
            subscriber.onSubscribe(listener);
            listener.start();
        }

        static final class Listener implements Subscription {
            private final Subscriber<? super Integer> subscriber;


            Listener(Subscriber<? super Integer> subscriber) {
                this.subscriber = subscriber;
            }

            public void start() {
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(2);
                subscriber.onNext(2);
                subscriber.onNext(3);
                subscriber.onNext(4);
                subscriber.onNext(5);
                subscriber.onNext(5);
            }

            @Override
            public void request(long n) {

            }

            @Override
            public void cancel() {
            }
        }
    }
}
