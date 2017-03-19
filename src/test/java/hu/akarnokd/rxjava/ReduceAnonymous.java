package hu.akarnokd.rxjava;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.mockito.*;

import io.reactivex.*;


public class ReduceAnonymous {
    @Test
    public void mutableStateWithStringConcat() throws Exception {
        boolean await = Observable.range(0, 1_000_000)
                .reduce("", (s, integer) -> s + integer)
                //.subscribeOn(Schedulers.computation())
                .test()
                .awaitTerminalEvent(5_000, TimeUnit.MILLISECONDS);

        Assert.assertTrue(await);
    }

    @Test
    public void mutableStateWithStringConcat2() throws Exception {
        @SuppressWarnings("unchecked")
        SingleObserver<String> mock = mock(SingleObserver.class);

        Observable.range(0, 1_000_000)
                .reduce("", (s, integer) -> s + integer)
                .subscribe(mock);

        InOrder inOrder = Mockito.inOrder(mock);

        inOrder.verify(mock, times(1))
                .onSuccess(any());
    }
    
    @Test
    public void howLong() {
        for (int i = 1; i <= 1_000_000; i *= 10) {
            Observable.range(0, i)
            .map(v -> v != 0 ? (long)Math.log10(v) + 1 : 1L)
            .compose(v -> hu.akarnokd.rxjava2.math.MathObservable.sumLong(v))
            .subscribe(System.out::println);
        }
    }
}
