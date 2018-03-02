package hu.akarnokd.rxjava2;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class JavaCompilerBugTest {
    @Test
    public void test() throws Exception {
        Observable<String> first = Observable.fromCallable(() -> "HEY").delay(250, TimeUnit.MILLISECONDS);
        Observable<Integer> second = Observable.fromCallable(() -> 1).delay(350, TimeUnit.MILLISECONDS);
        List<Observable<?>> observables = com.google.common.collect.Lists.newArrayList(first, second);
        Map<Long, Object> someWeirdMapWithObject = com.google.common.collect.ImmutableMap.of(
                1L, new BrandBuilder(1),
                2L, new BrandBuilder(2)
        );
        Observable
                .fromIterable(observables)
                .flatMap(task -> task.observeOn(Schedulers.computation()))
                // wait for all tasks to finish
                .lastOrError()
                .flattenAsObservable(x -> someWeirdMapWithObject.values())
                .<BrandBuilder>cast(BrandBuilder.class)
                .map(BrandBuilder::build)
                .toList().blockingGet();
    }

    class BrandBuilder{
        private int bar;
        BrandBuilder(int bar) {
            this.bar = bar;
        }
        public Brand build(){
            return new Brand(this.bar);
        }
    }

    class Brand{
        int bar;
        Brand(int bar) {
            this.bar = bar;
        }
    }
}
