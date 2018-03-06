package hu.akarnokd.rxjava2;

import org.junit.Test;

import io.reactivex.Single;

public class SingleUsingTest {

    @Test
    public void test() {
        Single.just("input").flatMap(val ->
                    Single.using(
                        () -> {
                            System.out.println("create");
                            return "";
                            },
                        call -> singleSubscriber -> {
                            System.out.println("sub");
                            singleSubscriber.onSuccess(val);
                            System.out.println("sub done");
                            },
                        call -> {
                            System.out.println("close");
                        }
                    )
                .retry((count, error) -> {
                    System.out.println("retry");
                    return true;
                })
        )
        .subscribe(val -> System.out.println("output " + val));
    }
}
