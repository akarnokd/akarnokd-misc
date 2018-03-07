package hu.akarnokd.rxjava;

import java.util.List;

import org.junit.Test;

import rx.*;
import rx.schedulers.Schedulers;

public class ListFlatMappingTest {

    Single<List<Integer>> getList() {
        return null;
    }
    
    Observable<Boolean> getSubdetails(Integer item) {
        return null;
    }
    
    @Test
    public void test() {
        getList()
        .subscribeOn(Schedulers.io())
        .flatMapObservable(list -> Observable.from(list))
        .flatMap(each -> getSubdetails(each))
        .subscribe(new Subscriber<Boolean>() {
            @Override
            public void onNext(Boolean t) {
            }
            @Override
            public void onError(Throwable error) {
            } 
            @Override
            public void onCompleted() {
            }
        });
    }
}
