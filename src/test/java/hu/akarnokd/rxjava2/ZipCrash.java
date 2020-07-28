package hu.akarnokd.rxjava2;

import java.util.*;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ZipCrash {
    @Test
    public void testUncaughtException() throws InterruptedException {
        Observable<Object> first = Observable.create(e -> {
            System.out.println("first");
            throw new Exception("first exception");
        });

        Observable<Object> second = Observable.create(e -> {
            System.out.println("second");
            throw new Exception("second exception");
        });

        List<Observable<?>> observableList = new ArrayList<>();
        observableList.add(first);
        observableList.add(second);

        Observable.zip(observableList, objects -> "result")
                .subscribeOn(Schedulers.io())
                .subscribe(
                        System.out::println,
                        t -> {
                            System.out.println("exception caught!");
                        }
                );
        Thread.sleep(2000);
    }
    
    @Test
    public void testUncaughtExceptionWithFlatMap() throws InterruptedException {
        Observable<Object> testObservable = Observable.create(e -> e.onNext(""))
                .flatMap((Function<Object, ObservableSource<?>>) o -> {

                    Observable<Object> first = Observable.create(e -> {
                        System.out.println("first");
                        throw new Exception("first exception");
                    });

                    Observable<Object> second = Observable.create(e -> {
                        System.out.println("second");
                        throw new Exception("second exception");
                    });

                    List<Observable<?>> observableList = new ArrayList<>();
                    observableList.add(first);
                    observableList.add(second);

                    return Observable.zip(observableList, objects -> "result");
                });

        testObservable
                .subscribeOn(Schedulers.io())
                .subscribe(
                        System.out::println,
                        t -> System.out.println("exception caught!")
                );

        Thread.sleep(2000);
    }
}
