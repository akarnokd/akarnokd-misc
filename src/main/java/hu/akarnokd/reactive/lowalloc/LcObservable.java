package hu.akarnokd.reactive.lowalloc;

import java.util.function.Function;

import io.reactivex.Observer;

public abstract class LcObservable<T> {

    public abstract void subscribe(Observer<? super T> observer);

    public static <T> LcObservable<T> just(T value) {
        return new LcObservableJust<T>(value);
    }

    public static LcObservable<Integer> range(int start, int count) {
        if (count == 1) {
            return just(start);
        }
        return new LcObservableRange(start, start + count);
    }

    public final <R> LcObservable<R> map(Function<T, R> mapper) {
        return new LcObservableMap<T, R>(this, mapper);
    }
}
