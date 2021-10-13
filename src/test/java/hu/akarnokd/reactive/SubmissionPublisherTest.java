package hu.akarnokd.reactive;

import java.util.concurrent.SubmissionPublisher;

import org.junit.Test;

public class SubmissionPublisherTest {

    @Test
    public void test() {
        SubmissionPublisher<Integer> sp = new SubmissionPublisher<>();
        sp.submit(1);
        sp.submit(2);
        sp.close();
        
        sp.consume(System.out::println).join();
    }
}
