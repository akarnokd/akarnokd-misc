package hu.akarnokd.iterables;

import java.util.Arrays;

import org.junit.Test;

import ix.Ix;

public class IxNulls {

    @Test
    public void test() {
        Ix.from(Arrays.<String>asList("1", null, "2", "3", null))
        .filter(v -> v != null)
        .subscribe(System.out::println);
    }
}
