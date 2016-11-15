package hu.akarnokd.rxjava.strings;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

import rx.Observable;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;

public class LinesBackpressure {

    @Test
    public void decode() {
        byte[] bytes = new byte[16];
        Arrays.fill(bytes, (byte)65);

        byte[] bytes2 = new byte[16];
        Arrays.fill(bytes, (byte)65);
        bytes[15] = 10;

        Observable<byte[]> data = Observable.just(bytes, bytes, bytes2).repeat(500);

        StringObservable.decode(data, StandardCharsets.UTF_8)
        .observeOn(Schedulers.computation(), false, 1)
        .toBlocking()
        .subscribe(System.out::println);
    }

}
