package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import rx.*;
import rx.schedulers.Schedulers;

public class BasicPrint2 {
    public static void main(String[] args) throws Exception {
        Observable.interval(10, TimeUnit.MILLISECONDS)
        .doOnNext(System.out::println)
        .observeOn(Schedulers.computation(), false, 10)
        .subscribe(new Subscriber<Long>() {

          @Override
          public void onStart() {
            request(1);
          }

          @Override
          public void onNext(Long t) {
            try {
              Thread.sleep(30);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            System.out.println("received: " + t);
            request(1L);
          }
          
          @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
          
          @Override
            public void onCompleted() {
                // TODO Auto-generated method stub
                
            }
        });
        
        Thread.sleep(100000);
    }
}
