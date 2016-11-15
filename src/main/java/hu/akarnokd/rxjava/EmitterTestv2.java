package hu.akarnokd.rxjava;

import io.reactivex.*;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public final class EmitterTestv2 {
    private EmitterTestv2() { }
  public static void main(String[] args) {
    Flowable<Integer> obs = Flowable.create(emitter -> {
      for (int i = 1; i < 1000; i++) {
        if (i % 5 == 0) {
          sleep(300L);
        }

        emitter.onNext(i);
      }

      emitter.onComplete();
    }, BackpressureStrategy.MISSING);

    obs.subscribeOn(Schedulers.computation())
        .onBackpressureLatest()
        .observeOn(Schedulers.computation(), false, 64)
        .blockingSubscribe(new Consumer<Integer>() {
            int i;
            @Override
            public void accept(Integer value) throws Exception {
                System.out.println((++i) + " Received " + value);
            }
        }); // Why does this get stuck at "Received 128"

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