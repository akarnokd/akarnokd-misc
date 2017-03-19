package hu.akarnokd.rxjava2;

import java.util.*;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.processors.*;
import io.reactivex.schedulers.Schedulers;

public class PagingFeedbackLoop {

    List<Integer> service(int index) {
        System.out.println("Reading " + index);
        List<Integer> list = new ArrayList<>();
        for (int i = index; i < index + 20; i++) {
            list.add(i);
        }
        return list;
    }
    
    Flowable<List<Integer>> getPage(int index) {
        FlowableProcessor<Integer> pager = UnicastProcessor.<Integer>create().toSerialized();
        pager.onNext(index);
        
        return pager.observeOn(Schedulers.trampoline(), true, 1)
        .map(v -> {
            List<Integer> list = service(v);
            pager.onNext(list.get(list.size() - 1) + 1);
            return list;
        })
        ;
    }

    @Test
    public void testPager() {
        getPage(0).take(20)
        .subscribe(System.out::println, Throwable::printStackTrace);
    }
}
