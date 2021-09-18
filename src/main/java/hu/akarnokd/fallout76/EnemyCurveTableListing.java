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
        Map<String, Ba2FileEntry> curveMap2 = new HashMap<>();
        EsmExport.loadJsonBa2(curveMap, basePath + "SeventySix - Startup.ba2");

        List<Ba2FileEntry> selection = new ArrayList<>();
        Set<String> variants = new HashSet<>();
        for (Ba2FileEntry e : curveMap.values()) {
            if (e.name.toLowerCase().contains("armor_") && e.name.toLowerCase().contains("creature") && e.name.toLowerCase().endsWith("json")) {
                selection.add(e);
                int idx = e.name.toLowerCase().indexOf("armor_");
                int jdx = e.name.toLowerCase().lastIndexOf("_");

                variants.add(e.name.substring(idx + 6, jdx));
                curveMap2.put(e.name.substring(idx), e);
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

        List<String> variantList = new ArrayList<>(variants);
        variantList.sort(Comparator.naturalOrder());
        System.out.printf("%n%n");

        String[] postfixes = { "dr", "er", "rad", "fire", "cold", "poison" };
        String[] postfixesRename = { "dr", "er", "rr", "fr", "cr", "pr" };
        System.out.println("var enemyResistancesMap = {");
        for (String e : variantList) {
            System.out.printf("  \"%s\": [%n", e);
            Map<String, JsonArray> arrays = new HashMap<>();
            for (String p : postfixes) {
                String key = "armor_" + e + "_" + p + ".json";
                Ba2FileEntry map = curveMap2.get(key);
                if (map != null) {
                    JsonElement obj = new JsonParser().parse(new String(map.data, StandardCharsets.ISO_8859_1));
                    JsonArray arr = obj.getAsJsonObject().get("curve").getAsJsonArray();
                    arrays.put(p, arr);
                } else {
                    arrays.put(p, new JsonArray());
                }
            }

            JsonArray def = arrays.get("dr");

            for (JsonElement o : def) {
                int x = o.getAsJsonObject().get("x").getAsInt();

                System.out.printf("    { level: %d", x);

                int k = 0;
                for (String p : postfixes) {
                    String q = postfixesRename[k];
                    System.out.printf(", %s: %d", q, find(arrays.get(p), x, 0));
                    k++;
                }

                System.out.printf(" }, %n");
            }
            System.out.println("  ],");
        }
        System.out.println("};");
    }

    static int find(JsonArray array, int x, int defaultX) {
        for (JsonElement o : array) {
            int x0 = o.getAsJsonObject().get("x").getAsInt();
            if (x0 == x) {
                return o.getAsJsonObject().get("y").getAsInt();
            }
        }

        return defaultX;
    }
}
