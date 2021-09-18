package hu.akarnokd.rxjava;

import org.junit.*;

import rx.*;
import rx.functions.Action1;
import rx.plugins.RxJavaHooks;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class AutoRemoveSubscription {

    public static <T> void subscribeAutoRelease(
            Observable<T> source,
            final Action1<T> onNext,
            CompositeSubscription composite) {
        Subscriber<T> subscriber = new Subscriber<T>() {
            @Override
            public void onCompleted() {
                composite.remove(this);
            }
            @Override
            public void onError(Throwable e) {
                composite.remove(this);
                RxJavaHooks.onError(e);
            }
            @Override
            public void onNext(T t) {
                try {
                    onNext.call(t);
                } catch (Throwable ex) {
                    unsubscribe();
                    onError(ex);
                }
            }
        };
        composite.add(subscriber);
        source.subscribe(subscriber);
    }

    @Test
    public void test() {
        CompositeSubscription composite = new CompositeSubscription();

        PublishSubject<Integer> ps = PublishSubject.create();

        subscribeAutoRelease(ps, System.out::println, composite);

        Assert.assertTrue(composite.hasSubscriptions());

        ps.onNext(1);
        ps.onCompleted();

        Assert.assertFalse(composite.hasSubscriptions());
    }
}
