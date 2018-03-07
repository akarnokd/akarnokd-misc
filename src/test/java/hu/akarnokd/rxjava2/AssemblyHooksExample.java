package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.internal.operators.observable.ObservableFromArray;
import io.reactivex.plugins.RxJavaPlugins;

public class AssemblyHooksExample {

    @Test
    public void test() {
    RxJavaPlugins.setOnObservableAssembly(o -> {
        if (o instanceof ObservableFromArray) {
            return new ObservableFromArray<>(new Integer[] { 4, 5, 6 });
        }
        return o;
    });

    Observable.just(1, 2, 3)
    .filter(v -> v > 3)
    .test()
    .assertResult(4, 5, 6);

    RxJavaPlugins.setOnObservableAssembly(null);
    }
}
