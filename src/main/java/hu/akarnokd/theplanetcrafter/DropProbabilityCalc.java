package hu.akarnokd.theplanetcrafter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DropProbabilityCalc {

    record Larvae(String name, double chance) { }
    
    static void RunMonteCarlo(Larvae... perItem) {
        Random rnd = new Random();
        
        int[] countItem = new int[perItem.length];
        int total = 0;
        int m = 50_000_000;
        
        for (int i = 0; i < m; i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < perItem.length; j++) {
                list.add(j);
            }
            
            while (list.size() > 0) {
                int entryIndex = rnd.nextInt(list.size());
                int jdx = list.get(entryIndex);
                double p = perItem[jdx].chance;
                
                if (p >= rnd.nextDouble()) {
                    countItem[jdx]++;
                    total++;
                    break;
                }
                list.remove(entryIndex);
            }
        }
        
        for (int i = 0; i < countItem.length; i++) {
            System.out.printf("%12s : %.6f%% | %10d / %10d / %10d = %,6f%%, %n", perItem[i].name, 100d * countItem[i] / m, countItem[i], total, m, 100d * countItem[i] / total);
        }
        if (m - total != 0) {
            System.out.printf("%12s : %.6f%% | %10d%n", "None", 100d * (m - total) / m, m - total);
        }
        System.out.printf("%n");
    }
    
    public static void main(String[] args) {
        System.out.println("Common");
        System.out.println("------");
        RunMonteCarlo(
                new Larvae("Azurae", 1),
                new Larvae("Leani", 0.75),
                new Larvae("Fensea", 0.5),
                new Larvae("Galaxe", 0.75),
                new Larvae("Abstreus", 0.2),
                new Larvae("Empalio", 1)
        );
        System.out.println("Uncommon");
        System.out.println("--------");
        RunMonteCarlo(
                new Larvae("Bee", 1),
                new Larvae("Silkworm", 0.7)// new Larvae("Silkworm", 0.8)
        );
        System.out.println("Rare");
        System.out.println("----");
        RunMonteCarlo(
                new Larvae("Penga", 0.7),
                new Larvae("Chevrone", 0.6),
                new Larvae("Aemel", 0.5),
                new Larvae("Liux", 0.5),
                new Larvae("Imeo", 0.8),
                new Larvae("Serena", 0.7),
                new Larvae("Golden", 0.15)
        );

        System.out.println("Base");
        System.out.println("----");
        RunMonteCarlo(
                new Larvae("Common", 1),
                new Larvae("Uncommon", 0.5),
                new Larvae("Rare", 0.2)
        );

        System.out.println("Dunes, Bassins, CrandCanyon, Cave-Grotte, ShroomRiver, BlackDesert, OrangeDesert");
        System.out.println("----");
        RunMonteCarlo(
                new Larvae("Common", 1),
                new Larvae("Uncommon", 0.5),
                new Larvae("Rare", 0.2),
                new Larvae("Special", 0.05)
        );

        System.out.println("Crater, Mont");
        System.out.println("----");
        RunMonteCarlo(
                new Larvae("Common", 1),
                new Larvae("Uncommon", 0.5),
                new Larvae("Rare", 0.2),
                new Larvae("Nere", 0.05),
                new Larvae("Fiorente", 0.05)
        );

        System.out.println("Waterfalls");
        System.out.println("----");
        RunMonteCarlo(
                new Larvae("Common", 1),
                new Larvae("Uncommon", 0.5),
                new Larvae("Rare", 0.2),
                new Larvae("Nere", 0.05),
                new Larvae("Lorpen", 0.05),
                new Larvae("Fiorente", 0.05)
        );
        
        System.out.println("CavesTop");
        System.out.println("----");
        RunMonteCarlo(
                new Larvae("Common", 1),
                new Larvae("Uncommon", 0.5),
                new Larvae("Rare", 0.2),
                new Larvae("Nere", 0.05),
                new Larvae("Lorpen", 0.05),
                new Larvae("Fiorente", 0.05),
                new Larvae("Alben", 0.05)
        );
        
        System.out.println("LostParadise");
        System.out.println("----");
        RunMonteCarlo(
                new Larvae("Golden", 0.15),
                new Larvae("Serena", 0.70),
                new Larvae("Fiorente", 0.05),
                new Larvae("Alben", 0.05),
                new Larvae("Lorpen", 0.05)
        );
    }
}
