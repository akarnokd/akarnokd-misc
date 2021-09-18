package hu.akarnokd.rxjava2;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.functions.*;
import io.reactivex.schedulers.Schedulers;

public class ConcatRange {

    @Test
    public void test() throws Exception {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                System.out.println("concatMap");
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + i + " of " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.single())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("concatMap: accept:" + s);
                    }
                });

        Thread.sleep(2000);
    }

    @Test
    public void test2() throws Exception {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }, BackpressureStrategy.BUFFER)
        .concatMap(new Function<Integer, Flowable<String>>() {
            @Override
            public Flowable<String> apply(Integer integer) throws Exception {
                System.out.println("concatMap");
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + i + " of " + integer);
                }
//                int delayTime = (int) (1 + Math.random() * 10);
                return Flowable.fromIterable(list);//.delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.single())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("concatMap: accept:" + s);
                    }
                });

        Thread.sleep(2000);
    }
}
