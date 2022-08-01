package hu.akarnokd.misc;

import java.util.ArrayList;
import java.util.List;

public class ExclusionForwardBack {

    static void compute(int[] start, int[] end, List<List<Integer>> results) {
        for (int i = 0; i < start.length; i++) {
            List<Integer> step = new ArrayList<>();
            for (int j = start[i]; j <= end[i]; j++) {
                step.add(j);
            }
            results.add(step);
        }
        
        for (int i = start.length - 2; i >= 0; i--) {
            for (int j = i; j >= 0; j--) {
                results.get(j).removeAll(results.get(i + 1));
            }
            
            List<Integer> current = results.get(i);
            for (int j = 0; j < current.size() - 1; j++) {
                if (current.get(j) + 1 != current.get(j + 1)) {
                    for (int k = j; k >= 0; k--) {
                        current.remove(k);
                    }
                    break;
                }
            }
        }
    }
    
    static void print(List<List<Integer>> results) {
        for (int i = 0; i < results.size(); i++) {
            System.out.println("Step " + i);
            for (int j : results.get(i)) {
                System.out.println("  " + j);
            }
        }
    }
    
    public static void main(String[] args) {
        int[] start1 = {1, 3, 4};
        int[] end1 = {6, 10, 5};

        List<List<Integer>> results1 = new ArrayList<>();
        
        compute(start1, end1, results1);
        print(results1);

        int[] start2 = {1, 4, 8, 12};
        int[] end2 = {5, 9, 14, 15};

        System.out.println("=======================");
        
        List<List<Integer>> results2 = new ArrayList<>();
        
        compute(start2, end2, results2);
        print(results2);

        System.out.println("=======================");

        int[] start3 = {1, 3, 5};
        int[] end3 = {6, 10, 6};

        List<List<Integer>> results3 = new ArrayList<>();
        
        compute(start3, end3, results3);
        print(results3);
    }
}
