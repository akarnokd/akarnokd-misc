package hu.akarnokd.rxjava;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Random;

import static rx.Observable.range;

public class GroupByTest2 {
    public static void main(String[] args) throws Exception {
        Observable<Integer> source = range(1, 10000);
        source
                .doOnRequest(i -> System.out.println("Requested " + i))
                .groupBy(v -> v % 5)
                .flatMap(g -> g.observeOn(Schedulers.io()).map(GroupByTest2::calculation), 4)
                .subscribe(i -> System.out.println("Got " + i));
        Thread.sleep(100000);
    }

    private static Integer calculation(Integer i) {
        sleep();
        System.out.println("Processing " + i);
        return i * 20;
    }

    private static void sleep() {
        try {
            Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}