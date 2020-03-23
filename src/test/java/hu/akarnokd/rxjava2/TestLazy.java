package hu.akarnokd.rxjava2;
import io.reactivex.Flowable;

public class TestLazy {
    public static void main(String[] args) {
        Flowable.just(1, 2, 3)
            .filter(s -> s > 4)
            .zipWith(Flowable.defer(() -> Flowable.just(get1(), get2(), get3())), (a, b) -> a + b)
            .subscribe(System.out::println);

        Flowable.just(1, 2, 3)
            .filter(s -> s > 4)
            .flatMap(a -> Flowable.defer(() -> Flowable.just(fromFlatMap())))
            .subscribe(System.out::println);
    }

    private static int fromFlatMap() {
        System.out.println("from flatMap");
        return 0;
    }

    private static int get1() {
        System.out.println("get 1");
        return 1;
    }

    private static int get2() {
        System.out.println("get 2");
        return 2;
    }

    private static int get3() {
        System.out.println("get 3");
        return 3;
    }
}