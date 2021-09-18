package hu.akarnokd.rxjava3;

import org.junit.Test;

import io.reactivex.rxjava3.core.Single;

public class NestedUsing {

    @Test
    public void test() {
        for (int i = 0; i < 2; i++) {
            int i0 = i;
            for (int j = 0; j < 2; j++) {
                int j0 = j;

                System.out.println("---");
                System.out.printf("Outer: %s, Inner %s%n", i == 0, j == 0);

                Single.using(() -> 1, v ->
                    Single.using(() -> 2,
                            w -> Single.just(1),
                            w -> System.out.println("Inner release, eager: " + (j0 == 0)),
                            j0 == 0),
                    v -> System.out.println("Outer release, eager: " + (i0 == 0)),
                    i == 0
                )
                .blockingSubscribe();
            }
        }
    }

}
