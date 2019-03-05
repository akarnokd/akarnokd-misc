package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.subjects.PublishSubject;

public class GroupByTimeout {
    @Test
    public void test() {
        PublishSubject<Integer> ps = PublishSubject.create();
        
        ps.groupBy(v -> v % 5).flatMap(g -> g.timeout(1, TimeUnit.HOURS))
        .subscribe(System.out::println);
        
        
    }
}
