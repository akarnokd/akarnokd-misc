package hu.akarnokd.rxjava3;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;

public class OnePerGroup {

    record SomeModel(
        int importantField1,
        int mainData
     ) {}
    
    
    @Test
    public void test() {
        Observable<String> names = Observable.just(
                "John", "Steve", "Ruth",
                "Sam", "Jane", "James");

        names.groupBy(s -> s.charAt(0))
        .flatMap(grp -> grp.publish(o -> o.firstElement().toObservable().concatWith(o.ignoreElements())))
        .subscribe(s -> System.out.println(s));
        
        List<SomeModel> dataList = new ArrayList<>();
        dataList.add(new SomeModel(3, 1));
        dataList.add(new SomeModel(3, 1));
        dataList.add(new SomeModel(2, 1));
        
        Function<Observable<SomeModel>, Observable<SomeModel>> f1 = o -> o.firstElement().toObservable().concatWith(o.ignoreElements());
        
        List<SomeModel> resultList = Observable.fromIterable(dataList)
                .sorted((s1, s2) -> Long.compare(s2.importantField1, s1.importantField1))
                .groupBy(s -> s.importantField1)
                .firstElement()
                // Some transformation to Observable. May be it is not elegant, but it is not a problem
                .flatMapObservable(item -> item)
                .groupBy(s -> s.mainData)
                //Till this line I have all I need. But then I need to take only one element from each branch
                .flatMap(grp -> grp.publish(o -> o.firstElement().toObservable().concatWith(o.ignoreElements())))
                .toList()
                .blockingGet();
        System.out.println(resultList);
    }
}
