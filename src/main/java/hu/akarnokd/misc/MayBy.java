package hu.akarnokd.misc;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MayBy {

    
    public static void main(String[] args) {

record MyObject(int stageNumber, int stageToCalc) { }

List<MyObject> list = List.of(
        new MyObject(2, 1),
        new MyObject(5, 1),
        new MyObject(9, 7),
        new MyObject(10, 7)
);

list.stream()
.collect(Collectors.groupingBy(v -> v.stageToCalc, TreeMap::new, Collectors.toList()))
.lastEntry()
.getValue()
.stream()
.collect(Collectors.groupingBy(v -> v.stageNumber, TreeMap::new, Collectors.toList()))
.lastEntry()
.getValue()
.forEach(System.out::println);
        ;
    }
}
