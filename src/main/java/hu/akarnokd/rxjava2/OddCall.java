package hu.akarnokd.rxjava2;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.*;
import io.reactivex.subjects.PublishSubject;

public final class OddCall {

    private OddCall() { }

    public static void main(String[] args) {
        PublishSubject<String> trigger = PublishSubject.create();

        Observable<String> obs1 = trigger.filter(new Predicate<String>() {
            @Override
            public boolean test(String s) throws Exception {
                return s != null;
            }
        });

        Observable<String> obs2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                //When/is this ever disposed?
                obs1.subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        System.out.println(s);
                        //Never called after obs1 is disposed.
                        e.onNext(s);
                    }
                });
            }
        });

        //A
        Disposable disposable = obs2.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println(s);
            }
        });


        trigger.onNext("Holahoop"); //Above "A" is called.

        disposable.dispose();

        trigger.onNext("Hakuna Matata"); //Nothing happens.
    }
}
