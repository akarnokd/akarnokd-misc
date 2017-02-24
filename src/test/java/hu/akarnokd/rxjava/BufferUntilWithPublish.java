package hu.akarnokd.rxjava;

import java.util.List;

import rx.Observable;
import rx.functions.*;
import rx.subjects.PublishSubject;

public class BufferUntilWithPublish {
    public static <T extends Object> Observable<List<T>> bufferUntil(
            Observable<T> source,
            final Func1<T, Boolean> bufferClosingCriteria) {

//        final Observable<T> published = source.delay(500, TimeUnit.MILLISECONDS).publish().refCount();
//        return published.buffer(new Func0<Observable<T>>() {
//            @Override
//            public Observable<T> call() {
//                return published.filter(bufferClosingCriteria);
//            }
//        });
//        return source.publish(o -> o.buffer(() -> o.filter(bufferClosingCriteria)));
        return source.publish(o -> {
            PublishSubject<Object> ps = PublishSubject.create();
            return o.buffer(() -> ps)
                    .mergeWith(Observable.defer(() -> {
                        o.filter(bufferClosingCriteria).subscribe(ps);
                        return Observable.empty();
                    })).filter(list -> !list.isEmpty());
        });
    }
    
    public static void main(String[] args) throws Exception {
        Character[] arr = {'a','b','c','d','e','c'};

        bufferUntil(rx.Observable.from(arr), new Func1<Character, Boolean>() {
            @Override
            public Boolean call(Character character) {
                return character == 'c';
            }
        }).subscribe(new Action1<List<Character>>() {
            @Override
            public void call(List<Character> o) {
                System.out.println(o);
                for (int i = 0; i < o.size(); ++i)
                   System.out.println("LL " + o.get(i).toString());
            }
        });
        
        Thread.sleep(2000);
    }
}
