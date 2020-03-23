package hu.akarnokd.rxjava;

import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import rx.Observable;
import rx.schedulers.Schedulers;

public class GoodBadMap {
    @Test
    public void tryObservableToMap() {
        for (int i = 0; i < 100; i++) {
            bad();
            good();
        }
    }

    private static void good() {
        System.out.println("GOOD CASE");
        String goodOutput =
                m(m(m(m(m(Observable.from(ImmutableList.of("a","b","c","d")), "list")
                .distinct(), "distinct")
                .flatMap(s ->
                        m(m(Observable.fromCallable(() -> getIntForString(s))
                                .subscribeOn(Schedulers.io()), "getInt " + s)
                                .map(intValue -> Pair.of(s, intValue)), "pair " + s)), "flatMap")
                .toMap(Pair::getKey, Pair::getValue), "toMap")
                .map(map -> map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n"))), "OUTER")
                .toBlocking()
                .toIterable().iterator().next();


        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nOutput:");
        System.out.println(goodOutput);
    }

    private static void bad() {
        System.out.println("BAD CASE");
        String badOutput =
                m(m(m(m(Observable.from(ImmutableList.of("a","b","c","d")), "list")
                .distinct(), "distinct")
                .flatMap(s ->
                        m(m(m(Observable.fromCallable(() -> getIntForString(s)).subscribeOn(Schedulers.io()), "getInt " + s)
                                .map(intValue -> Pair.of(s, intValue)), "pair " + s)
                                .toMap(Pair::getKey, Pair::getValue), "toMap " + s)), "flatMap")
                .map(map -> map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n"))), "OUTER")
                .toBlocking()
                .toIterable().iterator().next();


        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nOutput:");
        System.out.println(badOutput);
    }

    private static <T> Observable<T> m(final Observable<T> observable, final String name) {
        if (!name.equals("OUTER")) {
            return observable;
        }
        return observable
                .doOnSubscribe(() -> logRxLifecycleEvent(name, "subscribe"))
                .doOnError((ex) -> logRxLifecycleEvent(name, "error: " + ex.getMessage()))
                .doOnCompleted(() -> logRxLifecycleEvent(name, "complete"))
                .doOnTerminate(() -> logRxLifecycleEvent(name, "terminating"))
                .doAfterTerminate(() -> logRxLifecycleEvent(name, "terminated"))
                .doOnUnsubscribe(() -> logRxLifecycleEvent(name, "unsubscribe"));
    }

    private static void logRxLifecycleEvent(final String name, final String event) {
        System.out.println("\tRXLOG " + name + " observable " + event);
    }

    private static int getIntForString(String s) {
        switch(s) {
            case "a":
                return 1;
            case "b":
                return 2;
            case "c":
                return 3;
            case "d":
                return 4;
            default:
                return 0;
        }
    }
}
