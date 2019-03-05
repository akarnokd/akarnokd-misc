package hu.akarnokd.rxjava2;

import java.util.*;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.observable.ObservableCreate;

public class CreateFlatFlat {

    @Test
    public void test() {
        Observable<Integer> observable = ObservableCreate.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onNext(4);
                emitter.onComplete();
            }
        });
        Disposable disposable = observable.flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer i) throws Exception {
                List<String> list = new ArrayList<>(3);
                for(int j = 0; j < 3; j++) {
                    list.add("I am from apply:" + i);
                    //list.add("I am from apply:" + i + "_" + j);
                }
                log("apply: " + list);
                return Observable.fromIterable(list);
            }
        })
        .flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        emitter.onNext("flatMap--" + s);
                        emitter.onComplete();
                    }
                });
            }
        })
        .subscribe(
            s -> log("accept: " + s)
            , throwable -> log(throwable.getMessage())
            , () -> log("complete!")
        );
    }
    
    static void log(String s) {
        System.out.println(s);
    }
}
