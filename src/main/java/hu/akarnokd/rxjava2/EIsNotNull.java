package hu.akarnokd.rxjava2;

import io.reactivex.*;
import rx.Single;

public class EIsNotNull {

    public static void main(String[] args) {

        Single.create(e -> System.out.println(e == null)).subscribe();

        Maybe.create(e -> System.out.println(e == null)).subscribe();

        Completable.create(e -> System.out.println(e == null)).subscribe();

        Flowable.create(e -> System.out.println(e == null), BackpressureStrategy.MISSING).subscribe();

        Observable.create(e -> System.out.println(e == null)).subscribe();

    }
}
