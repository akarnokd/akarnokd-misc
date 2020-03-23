package hu.akarnokd.rxjava2;

import java.util.concurrent.Executors;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class RightThread {
    private static Scheduler singleThread(String threadName) {
        return Schedulers.from(Executors.newFixedThreadPool(1, r -> {
            Thread t = new Thread(r, threadName);
            t.setDaemon(true);
            return t;
        }));
    }

    public static void main(String[] args) {
        Scheduler genSched = singleThread("genThread");
        Scheduler mapSched = singleThread("mapThread");
        // execute on "genThread"
        Flowable.generate(RightThread::infiniteGenerator)
                .subscribeOn(genSched, true)
                // execute on "mapThread"
                .observeOn(mapSched, false)
                .concatMapMaybe(RightThread::mapping)
                .subscribeOn(mapSched, true)
                // execute on the thread that creates the pipeline, block it until finished
                .blockingForEach(RightThread::terminal);
    }

    private static int nb;
    /** Must execute on "genThread" thread. */
    private static void infiniteGenerator(Emitter<Integer> emitter) {
        print(nb, "infiniteGenerator");
        emitter.onNext(nb++);
        checkCurrentThread("genThread");
    }

    /** Must execute on "mapThread" thread. */
    private static Maybe<Integer> mapping(Integer s) {
        print(s, "mapping");
        checkCurrentThread("mapThread");
        return Maybe.just(s);
    }

    /** Must execute on "terminal" thread. */
    private static void terminal(Integer s) {
        print(s, "terminal");
        checkCurrentThread("main");
    }

    private static void print(int item, String method) {
        System.out.format("%d - %s - %s()%n", item, Thread.currentThread().getName(), method);
    }

    private static void checkCurrentThread(String expectedThreadName) throws IllegalStateException {
        String name = Thread.currentThread().getName();
        if (!name.equals(expectedThreadName)) {
            throw new IllegalStateException("Thread changed from '" + expectedThreadName + "' to '" + name + "'");
        }
    }
}
