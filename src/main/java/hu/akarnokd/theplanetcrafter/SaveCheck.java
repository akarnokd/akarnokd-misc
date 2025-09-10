package hu.akarnokd.theplanetcrafter;

import java.nio.file.*;
import java.util.*;

public class SaveCheck {

    public static void main(String[] args) throws Throwable {
        HashSet<String> gids = new HashSet<>();
        Set<String> ignore = Set.of(
                "Iron", "Cobalt", "Magnesium", "ice", "Uranim", "Iridium",
                "pod", "Sulfur", "Silicon", "Alloy", "Aluminium", "Zeolite",
                "Backpack1", "Backpack2", "Backpack3", "Backpack4", "Backpack5", 
                "wreckpilar", "astrofood", "astrofood2", "Algae1Seed",
                "TreeRoot"
        );
        
        for (var s : Files.readAllLines(Paths.get("c:\\Users\\akarnokd\\Downloads\\Survival-1 (10).json"))) {
            
            int i = s.indexOf("\"gId\":");
            if (i > 0) {
                int j = s.indexOf(",", i);
                
                String gid = s.substring(i + 7, j - 1);
                if (!ignore.contains(gid)) {
                    gids.add(gid);
                }
            }
        }

        var list = new ArrayList<>(gids);
        list.sort(Comparator.naturalOrder());
        
        list.forEach(System.out::println);
    }
}
