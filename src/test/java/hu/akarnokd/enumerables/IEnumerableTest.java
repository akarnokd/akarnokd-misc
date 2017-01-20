package hu.akarnokd.enumerables;

import java.util.*;

import org.junit.*;

import ix.Ix;

public class IEnumerableTest {

    @Test
    public void fromIterable() {
        IEnumerable.fromIterable(Arrays.asList(1, 2, 3))
        .assertResult(1, 2, 3);
    }

    @Test
    public void fromIterableEmpty() {
        IEnumerable.fromIterable(Arrays.asList())
        .assertResult();
    }

    @Test
    public void take() {
        IEnumerable.fromIterable(Arrays.asList(1, 2, 3))
        .take(2)
        .assertResult(1, 2);
    }

    @Test
    public void skip() {
        IEnumerable.fromIterable(Arrays.asList(1, 2, 3))
        .skip(2)
        .assertResult(3);
    }

    @Test
    public void first() {
        Assert.assertEquals(1, IEnumerable.fromIterable(Arrays.asList(1, 2, 3))
        .first().intValue());
    }

    @Test(expected = NoSuchElementException.class)
    public void firstEmpty() {
        IEnumerable.fromIterable(Arrays.asList())
        .first();
    }

    @Test
    public void flatMapIterator() {
        IEnumerable.fromIterable(Ix.range(1, 3))
        .flatMapIterable(v -> Ix.range(v, 2))
        .assertResult(1, 2, 2, 3, 3, 4);
    }

    @Test
    public void concatArray() {
        IEnumerable.concatArray(IEnumerable.just(1), IEnumerable.just(2))
        .assertResult(1, 2);
    }
}
