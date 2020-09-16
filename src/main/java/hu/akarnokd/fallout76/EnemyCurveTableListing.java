package hu.akarnokd.fallout76;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.gson.*;

public class EnemyCurveTableListing {

    public static void main(String[] args) throws Exception {
        String basePath = null;
        
        for (String s : EsmExport.basePaths) {
            File f = new File(s + "SeventySix.esm");
            if (f.canRead()) {
                basePath = s;
            }
        }
        
        if (basePath == null) {
            System.err.println("Game not found");
            return;
        }
        
        Map<String, Ba2FileEntry> curveMap = new HashMap<>();
        EsmExport.loadJsonBa2(curveMap, basePath + "SeventySix - Startup.ba2");
        
        List<Ba2FileEntry> selection = new ArrayList<>();
        for (Ba2FileEntry e : curveMap.values()) {
            if (e.name.toLowerCase().contains("armor_") && e.name.toLowerCase().contains("creature") && e.name.toLowerCase().endsWith("json")) {
                selection.add(e);
            }
        }
        
        selection.sort((a, b) -> a.name.compareTo(b.name));
        
        for (Ba2FileEntry e : selection) {
            int idx = e.name.toLowerCase().indexOf("armor_");
            System.out.printf("%s: %n", e.name.substring(idx));
            
            JsonElement obj = new JsonParser().parse(new String(e.data, StandardCharsets.ISO_8859_1));
            JsonArray arr = obj.getAsJsonObject().get("curve").getAsJsonArray();
            
            int[] count = { 0 };
            arr.forEach(c -> {
                double v = c.getAsJsonObject().get("y").getAsDouble();
                if (v > 0) {
                    if (count[0]++ == 5) {
                        count[0] = 0;
                        System.out.printf("%n");
                    }
                    System.out.printf(Locale.US, "    %s ~ %s, ", c.getAsJsonObject().get("x").getAsInt(), v);
                }
            });
            
            System.out.printf("%n");
        }
    }
}
