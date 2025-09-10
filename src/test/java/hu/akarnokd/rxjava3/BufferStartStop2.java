package hu.akarnokd.rxjava3;

import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;


public class BufferStartStop2 {

    @Test
    public void test() {
        Observable
        .fromArray(1,2,3,2,4,5,0,2,1,3,4,5,2,2,1,4,5)
        .publish(v -> v.buffer(
                v.filter(w -> w == 1), 
                u -> v.filter(w -> w == 5))
        )
        .subscribe(System.out::println);
    }
}
