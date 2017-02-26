package hu.akarnokd.rxjava2;

import io.reactivex.*;
import io.reactivex.functions.Consumer;

public final class MaybeConcatTest {

    private MaybeConcatTest() { }

    public static void main(String[] args) {
        Maybe<String> m1 = Maybe.create(new MaybeOnSubscribe<String>() {

            @Override public void subscribe(MaybeEmitter<String> e) throws Exception {
                System.out.println("m1 called");
                e.onSuccess("m1");
            }
        });
        Maybe<String> m2 = Maybe.create(new MaybeOnSubscribe<String>() {

            @Override public void subscribe(MaybeEmitter<String> e) throws Exception {
                System.out.println("m2 called");
                e.onSuccess("m2");
            }
        });
        /*Disposable subscribe = */Maybe.concat(m1, m2)
                .firstElement()
                .subscribe(new Consumer<String>() {

                    @Override public void accept(String t) throws Exception {
                        System.out.println(t);
                    }
                });
    }
}
