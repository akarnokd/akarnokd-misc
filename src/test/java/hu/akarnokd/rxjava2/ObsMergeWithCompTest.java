package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;

public class ObsMergeWithCompTest {

    @Test
    public void test() throws Exception {
        Completable c = Completable.create(emitter -> new Thread(() -> {
            System.out.println("Completable start");
            try {
                Thread.sleep(1000);
                emitter.onComplete();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }).start());

        Observable.just(1).delay(500, TimeUnit.MILLISECONDS)
        .mergeWith(c)
        .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));

        // Thread.sleep(2000);
    }

    public static void main(String[] args) {
        Completable c = Completable.create(emitter -> new Thread(() -> {
            System.out.println("Completable start");
            try {
                Thread.sleep(1000);
                emitter.onComplete();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }).start());

        Observable.just(1).delay(500, TimeUnit.MILLISECONDS)
        .mergeWith(c)
        .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
    }
}
