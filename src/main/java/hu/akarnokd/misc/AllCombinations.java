package hu.akarnokd.misc;

import java.util.ArrayList;
import java.util.List;

public class AllCombinations {

    static void AddCombination(List<List<String>> source, int depth, 
            String prefix, List<String> output) {
        for (String layer : source.get(depth)) {
            String str = prefix + layer;
            if (depth < source.size() - 1) {
                AddCombination(source, depth + 1, str, output);
            } else {
                output.add(str);
            }
        }
    }
    
    public static void main(String[] args) {
        List<List<String>> source = List.of(
                List.of("a", "b"),
                List.of("A", "B", "C"),
                List.of("1", "2", "3", "4"));
        
        List<String> output = new ArrayList<>();
        
        AddCombination(source, 0, "", output);
        
        output.forEach(System.out::println);
    }
}
