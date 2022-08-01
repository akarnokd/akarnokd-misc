package hu.akarnokd.misc;

import java.util.List;
import java.util.stream.Collectors;

public class ParallelSum {

    public static void main(String[] args) {
        List.of(1, 2, 3, 4, 5)
        .parallelStream()
        .collect(Collectors.summingInt(v -> v));
    }
}
