package hu.akarnokd.planb;

import java.nio.file.*;
import java.util.*;

public class MergeLabels {

    public static void main(String[] args) throws Throwable {
        List<String> mainLines = Files.readAllLines(Paths.get("c:\\Users\\akarnokd\\git\\plan-b-terraform-mods\\UITranslationHungarian\\labels-Hungarian.txt"));
        
        List<String> oldLines = Files.readAllLines(Paths.get("c:\\Users\\akarnokd\\Downloads\\pbt_hun.txt"));
        Map<String, String> oldMap = new HashMap<>();
        for (var s : oldLines) {
            if (s.startsWith("#")) {
                continue;
            }
            int idx = s.indexOf('=');
            if (idx >= 0) {
                var key = s.substring(0, idx);
                oldMap.put(key, s);
            }
        }
        
        List<String> combinedLines = new ArrayList<>();
        
        for (var s : mainLines) {
            combinedLines.add(s);
            if (s.startsWith("#")) {
                combinedLines.add("# -------------------------------------------------------------------------------------");
            } else {
                int idx = s.indexOf('=');
                if (idx >= 0) {
                    var key = s.substring(0, idx).trim();
                    var value = oldMap.get(key);
                    if (value != null) {
                        combinedLines.add(value);
                    } else {
                        combinedLines.add(key + "=^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    }
                } else {
                    combinedLines.add("# -------------------------------------------------------------------------------------");
                }
            }
            combinedLines.add("");
        }
        
        Files.write(Paths.get("c:\\Users\\akarnokd\\Downloads\\pbt_hun_new.txt"), combinedLines);
    }
}
