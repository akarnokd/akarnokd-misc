package hu.akarnokd.rxjava3;

import java.time.LocalTime;
import java.util.concurrent.*;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class FairScheduler {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Scheduler scheduler = Schedulers.from(executor, false, true);
        PublishSubject<String> subject1 = PublishSubject.create();
        PublishSubject<String> subject2 = PublishSubject.create();

        subject1.delay(0, TimeUnit.MILLISECONDS, scheduler).subscribe(FairScheduler::log);
        subject2.delay(0, TimeUnit.MILLISECONDS, scheduler).subscribe(FairScheduler::log);

        subject1.onNext("Hello11");
        subject2.onNext("Hello21");
        subject1.onNext("Hello12");
        subject2.onNext("Hello22");
        subject1.onNext("Hello13");
        subject2.onNext("Hello23");

        log("Test activity");
    }
    
    static void log(Object o) {
        System.out.println(LocalTime.now() + " " + o);
    }
}
