package hu.akarnokd.rxjava;

import rx.*;
import rx.schedulers.Schedulers;

public final class EmitterTest {
    private EmitterTest() { }
  public static void main(String[] args) {
    Observable<Integer> obs = Observable.create(emitter -> {
      for (int i = 1; i < 1000; i++) {
        if (i % 5 == 0) {
          sleep(300L);
        }

        emitter.onNext(i);
      }

      emitter.onCompleted();
    }, Emitter.BackpressureMode.LATEST);

    obs.subscribeOn(Schedulers.computation())
        .observeOn(Schedulers.computation())
        .toBlocking()
        .subscribe(value -> System.out.println("Received " + value)); // Why does this get stuck at "Received 128"

//    sleep(10000L);
  }

  private static void sleep(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}