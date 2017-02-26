package hu.akarnokd.rxjava;

import org.junit.Test;

import rx.subjects.PublishSubject;

public class PublishBufferZip {

    @Test
    public void normal() {
PublishSubject<Integer> boundary = PublishSubject.create();

PublishSubject<Integer> source = PublishSubject.create();

boundary.publish(b -> source.buffer(b).zipWith(b, (list, e) -> { list.add(e); return list; }))
.subscribe(System.out::println);

source.onNext(1);
source.onNext(2);
source.onNext(3);

boundary.onNext(100);

source.onNext(4);
source.onNext(5);

boundary.onNext(200);

source.onNext(6);
boundary.onNext(300);

boundary.onNext(400);
    }
}
