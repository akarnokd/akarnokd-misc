package hu.akarnokd.rxjava2;

import java.time.LocalTime;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

public class RxLosers {

    @Test
    public void test() throws Exception {
        printWithTime("Starting.");
        Observable<String> obs = Observable.<String>create(e -> {
                    slowOperation();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .cache()
                ;
        ConnectableObservable<String> hot = obs.publish();
        hot.connect();
        printWithTime("Published and connected.");
        //Thread.sleep(3000);

        printWithTime("Subscribe.");
        hot.subscribe(this::printWithTime, Throwable::printStackTrace, () -> System.out.println("Done"));
        printWithTime("Subscribe2.");
        hot.subscribe(this::printWithTime, Throwable::printStackTrace, () -> System.out.println("Done"));
        Thread.sleep(3000);
    }

    void printWithTime(String msg)
    {
        System.out.println(LocalTime.now().toString() + ": " + msg);
    }

    String slowOperation() throws Exception
    {
        printWithTime("Starting long operation");
        Thread.sleep(1000);
        printWithTime("Finished long operation");
        return "Good";
    }
}
