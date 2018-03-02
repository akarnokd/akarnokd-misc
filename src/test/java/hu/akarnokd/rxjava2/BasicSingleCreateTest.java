package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.functions.Consumer;

public class BasicSingleCreateTest {

    @Test
    public void test() {
        Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                e.onSuccess("something");
            }
        })
        .subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) throws Exception {
                System.out.print(o);
            }
        });
    }
}
