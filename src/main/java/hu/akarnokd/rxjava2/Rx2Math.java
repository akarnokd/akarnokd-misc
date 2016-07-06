package hu.akarnokd.rxjava2;

import io.reactivex.*;

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
    
    public static Flowable<Integer> sumInt(Flowable<Integer> source) {
        return new SumIntFlowable(source);
    }
    
    public static Flowable<Long> sumLong(Flowable<Long> source) {
        return new SumLongFlowable(source);
    }
    
    public static Flowable<Integer> maxInt(Flowable<Integer> source) {
        return new MaxIntFlowable(source);
    }

}
