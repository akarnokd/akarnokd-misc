package hu.akarnokd.windmill;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import io.windmill.core.*;

public class WindmillTest {
    @Test
    public void million() throws Exception {
        CPUSet set = CPUSet.builder().addSocket(0).build();
        set.start();
        CPU cpu = set.get(0);
        
        CountDownLatch cdl = new CountDownLatch(1);
        int c = 1_000_000;
        
        for (int i = 0; i < c; i++) {
            int j = i;
            cpu.schedule(() -> {
                if (j % 1000 == 0) {
                    System.out.println("-- " + j);
                }
                if (j == c - 1) {
                    cdl.countDown();
                }
            });
        }
        
        cdl.await();
        set.halt();
    }
}
