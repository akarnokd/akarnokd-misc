package hu.akarnokd.rxjava2;

import java.util.concurrent.CancellationException;

import org.junit.Test;

import io.reactivex.*;

public class SingleTakeUntilTest {

    @Test
    public void test() {
        Single.just(1).takeUntil(Flowable.just(1).take(1))
        .test()
        .assertFailure(CancellationException.class);
    }
}
