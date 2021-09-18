package hu.akarnokd.rxjava2;

    import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

    public class BehaviorSubjectCombineLatest {

        BehaviorSubject<String> subject1;
        BehaviorSubject<String> subject2;

        @Test
        public void test() throws Exception {
            subject1 = BehaviorSubject.createDefault("hello");
            subject2 = BehaviorSubject.createDefault("goodbye");

            subject1.subscribe(v -> System.out.println("Subject1 it1: " + v));

            click();

            Thread.sleep(1);

            click();

            Thread.sleep(1);

            click();
        }

        void click() {
            subject1.onNext("hello button clicked " + System.currentTimeMillis());

            Observable.combineLatest(subject1, subject2, (a, b) -> "biFun call " + a + ", " + b)
            .subscribe(v -> System.out.println("Combined latest: " + v));
        }
    }
