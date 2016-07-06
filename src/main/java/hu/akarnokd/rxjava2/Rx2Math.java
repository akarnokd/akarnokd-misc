package hu.akarnokd.rxjava2;

import io.reactivex.Observable;

public enum Rx2Math {
    ;

    public static Observable<Integer> sumInt(Observable<Integer> source) {
        return new SumIntObservable(source);
    }
    
    public static Observable<Long> sumLong(Observable<Long> source) {
        return new SumLongObservable(source);
    }
    
    public static Observable<Integer> maxInt(Observable<Integer> source) {
        return new MaxIntObservable(source);
    }
}
