package hu.akarnokd.rxjava2;

import java.util.*;
import java.util.function.Supplier;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.subjects.*;

public class WithNextSupplierTest {

    @Test
    public void test() {
    Subject<String> firstSubject = PublishSubject.create();
    Supplier<Observable<String>> firstSupplier = () -> {
        System.out.println("side effect of first one");
        return firstSubject;
    };

    Subject<String> secondSubject = PublishSubject.create();
    Supplier<Observable<String>> secondSupplier = () -> {
        System.out.println("side effect of second one");
        return secondSubject;
    };

    Subject<String> thirdSubject = PublishSubject.create();
    Supplier<Observable<String>> thirdSupplier = () -> {
        System.out.println("side effect of third one");
        return thirdSubject;
    };

    Collection<Supplier<Observable<String>>> collection = Arrays.asList(firstSupplier, secondSupplier, thirdSupplier);

    Observable.fromIterable(collection)
    .concatMap(supplier -> supplier.get().take(1))
    .subscribe();

    System.out.println("// output: side effect of first one");
    firstSubject.onNext("");
    System.out.println("// output: side effect of second one");
    secondSubject.onNext("");
    System.out.println("// output: side effect of third one");
    }
}
