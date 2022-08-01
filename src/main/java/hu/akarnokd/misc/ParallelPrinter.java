package hu.akarnokd.misc;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallelPrinter {
    public static class Print implements Runnable {
        private String string;

        private CyclicBarrier cb = new CyclicBarrier(2);
        private CountDownLatch cdl = new CountDownLatch(1);
        private AtomicBoolean win = new AtomicBoolean();

        Print(String string) {
            this.string = string;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            for(int i = string.length()-1; i >= 0; i--) {
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                if (i != 0) {
                    System.out.print(string.charAt(i) + "-");
                }
                else {
                    if (win.getAndSet(true)) {
                        try {
                            cdl.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.print(string.charAt(i));
                    } else {
                        System.out.print(string.charAt(i) + "-");
                        cdl.countDown();
                    }
                }
            }

        }

    }

    public static void main(String[] args) {
        Print r = new Print("NAME");
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();    
    }
}
