package hu.akarnokd.rxjava2;

import static reactor.core.publisher.Flux.just;
import static reactor.core.scheduler.Schedulers.*;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import reactor.core.publisher.ConnectableFlux;

public class ConnectConnect {
    @Test(timeout = 5000)
    public void schedulerTest() throws InterruptedException {
      CountDownLatch latch = new CountDownLatch(2);

      ConnectableFlux<Integer> connectableFlux = just(1, 2, 3, 4, 5).publish();

      connectableFlux
          .log()
          .subscribe(v -> {} , e -> {}, latch::countDown);

      connectableFlux
          .publishOn(parallel())
          .log()
          .map(v -> v * 10)
          .publishOn(single())
          .log()
          .subscribeOn(elastic())
          .subscribe(v -> {} , e -> {}, latch::countDown);

      Thread.sleep(100);

      connectableFlux.connect();
      latch.await();
    }

    @Test(timeout = 5000)
    public void schedulerTestRx() throws InterruptedException {
      CountDownLatch latch = new CountDownLatch(2);

      ConnectableFlowable<Integer> connectableFlux = Flowable.just(1, 2, 3, 4, 5).publish();

      connectableFlux
          .doOnEach(System.out::println)
          .subscribe(v -> {} , e -> {}, latch::countDown);

      connectableFlux
          .observeOn(Schedulers.computation())
          .doOnEach(System.out::println)
          .map(v -> v * 10)
          .observeOn(Schedulers.single())
          .doOnEach(System.out::println)
          .subscribeOn(Schedulers.io())
          .subscribe(v -> {} , e -> {}, latch::countDown);

      Thread.sleep(100);
      
      connectableFlux.connect();
      latch.await();
    }
}
