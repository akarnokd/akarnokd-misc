package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class TrackSubscriber {

    public static void main(String[] args) throws Exception {
    RxJavaPlugins.setOnObservableSubscribe((observable, observer) -> {
        if (!observable.getClass().getName().toLowerCase().contains("map")) {
            return observer;
        }

        System.out.println("Started");

        class SignalTracker implements Observer<Object>, Disposable {
            Disposable upstream;
            @Override public void onSubscribe(Disposable d) {
                upstream = d;
                // write the code here that has to react to establishing the subscription
                observer.onSubscribe(this);
            }
            @Override public void onNext(Object o) {
                // handle onNext before or aftern notifying the downstream
                observer.onNext(o);
            }
            @Override public void onError(Throwable t) {
                // handle onError
                observer.onError(t);
            }
            @Override public void onComplete() {
                // handle onComplete
                System.out.println("Completed");
                observer.onComplete();
            }
            @Override public void dispose() {
                // handle dispose
                upstream.dispose();
            }
            @Override public boolean isDisposed() {
                return upstream.isDisposed();
            }
        }
        return new SignalTracker();
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
