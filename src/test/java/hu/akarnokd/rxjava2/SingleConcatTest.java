package hu.akarnokd.rxjava2;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

public class SingleConcatTest {
    @Test
    public void test() {
      TestScheduler testScheduler = new TestScheduler();

      final Single<List<Integer>> first = Single.timer(2, TimeUnit.SECONDS, testScheduler)
              .map(u -> Arrays.asList(1, 2, 3));
      final Single<List<Integer>> second = Single.just(Collections.emptyList());
      final Single<List<Integer>> third = Single.just(Collections.singletonList(4));
      final Single<List<Integer>> fourth = Single.just(Collections.singletonList(5));

      Single<List<Integer>> subject = Observable
        .fromIterable(Arrays.asList(first, second, third, fourth))
        .concatMapSingle(single -> single)
        .reduce(new ArrayList<>(), (seed, items) -> {
          seed.addAll(items);
          return seed;
        });

        TestObserver<List<Integer>> testObserver = subject.test();
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS);

        System.out.println(testObserver.values());
        testObserver.assertValue(list -> list.equals(Arrays.asList(1, 2, 3, 4, 5))); 
        // 5 is currently missing ; fourth was never subscribed in the first place
    }
}
