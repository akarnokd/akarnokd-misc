package hu.akarnokd.rxjava;

import java.util.concurrent.TimeUnit;

import rx.*;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

public class TrackSubscriber1 {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
    RxJavaHooks.setOnObservableStart((observable, onSubscribe) -> {
        if (!onSubscribe.getClass().getName().toLowerCase().contains("map")) {
            return onSubscribe;
        }

        System.out.println("Started");

        return (Observable.OnSubscribe<Object>)observer -> {
            class SignalTracker extends Subscriber<Object> {
                @Override public void onNext(Object o) {
                    // handle onNext before or aftern notifying the downstream
                    observer.onNext(o);
                }
                @Override public void onError(Throwable t) {
                    // handle onError
                    observer.onError(t);
                }
                @Override public void onCompleted() {
                    // handle onComplete
                    System.out.println("Completed");
                    observer.onCompleted();
                }
            }
            SignalTracker t = new SignalTracker();
            onSubscribe.call(t);
        };
      });

      Observable<Integer> observable = Observable.range(1, 5)
          .subscribeOn(Schedulers.io())
          .observeOn(Schedulers.computation())
          .map(integer -> {
            try {
              TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            return integer * 3;
          });

      observable.subscribe(System.out::println);

      Thread.sleep(6000L);
    }
}
