package hu.akarnokd.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpandKeysInMap {

    public static void main(String[] args) {
Map<List<String>, String> map = new HashMap<>();
map.put(List.of("A", "B"), "X");
map.put(List.of("C"), "Y");

var result = map.entrySet()
.stream()
.flatMap(kv -> kv.getKey().stream().map(k -> Map.entry(k, kv.getValue())))
.collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

result.entrySet().forEach(System.out::println);
    }
}
