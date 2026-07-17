package hu.akarnokd.theplanetcrafter;

import java.nio.file.*;
import java.util.HashSet;

public class DistinctPlanets {

    public static void main(String[] args) throws Throwable {
        System.out.println(GetStableHashCode("Toxicity"));

        var lines = Files.readAllLines(Paths.get("c:\\Users\\akarnokd\\AppData\\LocalLow\\MijuGames\\Planet Crafter\\Welt-1.json" ));
        var set = new HashSet<String>();

        for (var s : lines) {
            int idx = s.indexOf("\"planet\":");
            if (idx > 0) {
                int j = s.indexOf(",", idx);
                if (j < 0) {
                    j = s.indexOf("}", idx);
                }
                set.add(s.substring(idx + 9, j));
            }
        }
        System.out.println(set);
    }
    
    static int GetStableHashCode(String s)
    {
        int num = 5381;
        int num2 = num;
        int num3 = 0;
        var str = s.toCharArray();
        while (num3 < str.length && str[num3] != '\0')
        {
            num = ((num << 5) + num) ^ (int)str[num3];
            if (num3 == str.length - 1 || str[num3 + 1] == '\0')
            {
                break;
            }
            num2 = ((num2 << 5) + num2) ^ (int)str[num3 + 1];
            num3 += 2;
        }
        return num + num2 * 1566083941;
    }
}
