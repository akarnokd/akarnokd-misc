package hu.akarnokd.stream;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class StreamFilterMapCollect {

    public static void main(String[] args) {
         new ArrayList<Integer>().stream()
                .filter(i -> i % 2 == 0)
                .map(Math::sqrt)
                .collect(Collectors.toList());

    }
}
