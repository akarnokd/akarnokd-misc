package hu.akarnokd.rxjava2;

import io.reactivex.*;
import io.reactivex.functions.*;

public class ResumeFilter {
    public static <T> ObservableTransformer<T, T> onErrorResumeNextFilter(
            Predicate<? super Throwable> predicate, final ObservableSource<? extends T> next) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> o) {
                return o.onErrorResumeNext(new Function<Throwable, ObservableSource<? extends T>>() {
                    @Override
                    public ObservableSource<? extends T> apply(Throwable e) throws Exception {
                       if (predicate.test(e)) {
                           return next;
                       }
                       return Observable.error(e);
                    }
                });
            }
        };
    }

    public static <T> ObservableTransformer<T, T> onErrorReturnFilter(
            Predicate<? super Throwable> predicate, final T item) {
        return onErrorResumeNextFilter(predicate, Observable.just(item));
    }
}
