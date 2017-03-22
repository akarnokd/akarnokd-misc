package hu.akarnokd.reactive.observable;

import org.junit.*;

import hu.akarnokd.reactive.observables.IObservable;

public class IObservableTest {

    @Test
    public void just() {
        Assert.assertEquals((Integer)1, IObservable.just(1).first());
    }

    @Test
    public void characters() {
        Assert.assertEquals((Integer)(int)'d', IObservable.characters("abcd").last());
    }

    @Test
    public void concatArray() {
        Assert.assertEquals("d", IObservable.concatArray(IObservable.just("a"), IObservable.just("d")).last());
    }
}
