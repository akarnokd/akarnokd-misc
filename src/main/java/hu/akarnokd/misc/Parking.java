package hu.akarnokd.misc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Parking {
    public static void main(String[] args) throws InterruptedException {
/*
        CountDownLatch c = new CountDownLatch(1);
        Thread t1 = new Thread(() -> {
            try {
                System.out.println("t1 started");
                c.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("park...");
            LockSupport.park();
            System.out.println("resume...");
        });
        t1.start();
        LockSupport.unpark(t1);
        Thread.sleep(3000);
        System.out.println("unpark...");
        c.countDown();
*/
        for (int i = 0; i < 10000; i++) {
            AtomicInteger sync = new AtomicInteger(2);
            Thread t1 = new Thread(() -> {
                System.out.println("t1 started");
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) ;
                }
                System.out.println("park...");
                LockSupport.park();
                LockSupport.park();
                System.out.println("resume...");
            });
            t1.start();
            LockSupport.unpark(t1);
            LockSupport.unpark(t1);
            System.out.println("unpark..." + i);
            
            if (sync.decrementAndGet() != 0) {
                while (sync.get() != 0) ;
            }
        }
    }
}
