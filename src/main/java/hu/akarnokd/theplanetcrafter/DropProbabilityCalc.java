package hu.akarnokd.theplanetcrafter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DropProbabilityCalc {

    public static void main(String[] args) {
        Random rnd = new Random();
        
        double[] perItem = { 0.5, 0.5, 0.5 };
        int[] countItem = { 0, 0, 0 };
        int total = 0;
        int m = 10_000_000;
        
        for (int i = 0; i < m; i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < perItem.length; j++) {
                list.add(j);
            }
            
            while (list.size() > 0) {
                int entryIndex = rnd.nextInt(list.size());
                int jdx = list.get(entryIndex);
                double p = perItem[jdx];
                
                if (p >= rnd.nextDouble()) {
                    countItem[jdx]++;
                    total++;
                    break;
                }
                list.remove(entryIndex);
            }
        }
        
        for (int i = 0; i < countItem.length; i++) {
            System.out.printf("%d / %d / %d = %,6f%%, %,6f%%%n", countItem[i], total, m, 100d * countItem[i] / total, 100d * countItem[i] / m);
        }
        System.out.printf("None: %d = %,6f%%%n", m - total, 100d * (m - total) / m);
    }
}
