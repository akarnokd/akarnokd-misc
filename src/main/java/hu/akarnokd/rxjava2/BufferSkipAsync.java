package hu.akarnokd.rxjava2;

import io.reactivex.Observable;
import io.reactivex.subjects.*;
import ix.Ix;

public class BufferSkipAsync {

    public static void main(String[] args) {

        Subject<Integer> rp = PublishSubject.<Integer>create().toSerialized();

        Observable<Integer> share = rp.share();

        int numPrevious = 2;

        share.buffer(numPrevious, 1)
        .filter(b -> b.size() < numPrevious || b.get(numPrevious - 1) >= 3)
        .map(v -> v.get(0))
        .subscribe(System.out::println);

        Ix.range(1, 10).foreach(rp::onNext);
        rp.onComplete();
    }
}
