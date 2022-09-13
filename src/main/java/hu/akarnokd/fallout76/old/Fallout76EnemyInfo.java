package hu.akarnokd.fallout76.old;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hu.akarnokd.fallout76.Ba2File;
import hu.akarnokd.fallout76.Ba2FileEntry;
import io.reactivex.functions.BiConsumer;

public class Fallout76EnemyInfo {

    public static void main(String[] args) throws Exception {
        File file = new File(
                "c:\\Program Files (x86)\\Bethesda.net Launcher\\games\\Fallout76\\Data\\SeventySix - Startup.ba2");

        Ba2File ba2 = new Ba2File();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            ba2.read(raf, n -> false);

            Map<String, JsonElement> armorJson = new HashMap<>();
            Map<String, JsonElement> healthJson = new HashMap<>();
            Map<String, JsonElement> weaponJson = new TreeMap<>();

            for (Ba2FileEntry entry : ba2.entries) {
                if (entry.name.endsWith(".json")) {
                    int idx = entry.name.indexOf("health_");
                    if (idx > 0) {
                        String creature = entry.name.substring(idx + 7);

                        creature = creature.substring(0, creature.length() - 5);

//                        System.out.println("Health: " + creature);

                        raf.seek(entry.offset);
                        byte[] data = new byte[entry.size];
                        raf.read(data);

                        JsonElement obj = new JsonParser().parse(new String(data, StandardCharsets.ISO_8859_1));

                        healthJson.put(creature, obj);
                    }
                    idx = entry.name.indexOf("armor_");
                    if (idx > 0) {
                        String creature = entry.name.substring(idx + 6);

                        creature = creature.substring(0, creature.length() - 5);

//                        System.out.println("Armor: " + creature);

                        raf.seek(entry.offset);
                        byte[] data = new byte[entry.size];
                        raf.read(data);

                        JsonElement obj = new JsonParser().parse(new String(data, StandardCharsets.ISO_8859_1));

                        armorJson.put(creature, obj);
                    }
                    idx = entry.name.indexOf("weap_");
                    if (idx > 0) {
                        String weap = entry.name.substring(idx + 5);
                        weap = weap.substring(0, weap.length() - 8);

                        raf.seek(entry.offset);
                        byte[] data = new byte[entry.size];
                        raf.read(data);

                        JsonElement obj = new JsonParser().parse(new String(data, StandardCharsets.ISO_8859_1));
                        weaponJson.put(weap, obj);
                    }
                }
            }

            Map<String, Map<Integer, HealthArmor>> creatures = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : healthJson.entrySet()) {

                String name = entry.getKey();

                JsonElement drJson = armorJson.get(name + "_dr");
                JsonElement erJson = armorJson.get(name + "_er");
                JsonElement rrJson = armorJson.get(name + "_rad");

                if (drJson == null || erJson == null || rrJson == null) {
                    continue;
                }

                Map<Integer, HealthArmor> creature = creatures.computeIfAbsent(entry.getKey(), s -> new TreeMap<>());


                processCurveTable(entry.getValue(), (x, y) -> {
                    HealthArmor level = creature.computeIfAbsent(x, v -> new HealthArmor());

                    level.health = y;
                });

                processCurveTable(drJson, (x, y) -> {
                    HealthArmor level = creature.computeIfAbsent(x, v -> new HealthArmor());

                    level.dr = y;
                });

                processCurveTable(erJson, (x, y) -> {
                    HealthArmor level = creature.computeIfAbsent(x, v -> new HealthArmor());

                    level.er = y;
                });

                processCurveTable(rrJson, (x, y) -> {
                    HealthArmor level = creature.computeIfAbsent(x, v -> new HealthArmor());

                    level.rr = y;
                });

                /*
                System.out.println(name);

                for (Map.Entry<Integer, HealthArmor> e : creature.entrySet()) {
                    HealthArmor ha = e.getValue();
                    System.out.printf("    %3d:  %5d    %4d    %4d    %4d%n", e.getKey(), ha.health, ha.dr, ha.er, ha.rr);
                }
                */

                for (Map.Entry<Integer, HealthArmor> e : creature.entrySet()) {
                    HealthArmor ha = e.getValue();
                    System.out.printf("%s\t%d\t%d\t%d\t%d\t%d%n", name, e.getKey(), ha.health, ha.dr, ha.er, ha.rr);
                }
            }

            Map<String, Map<Integer, Integer>> weapons = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : weaponJson.entrySet()) {

                String name = entry.getKey();

                if (name.startsWith("mod_all_") || name.startsWith("template_")) {
                    continue;
                }

                Map<Integer, Integer> weapon = weapons.computeIfAbsent(name, s -> new TreeMap<>());

                processCurveTable(entry.getValue(), (x, y) -> {
                    if (y > 0) {
                        weapon.put(x, y);
                    }
                });

                for (Map.Entry<Integer, Integer> e : weapon.entrySet()) {
                    System.out.printf("%s\t%d\t%d%n", name, e.getKey(), e.getValue());
                }
}
        }
    }

    static void processCurveTable(JsonElement file, BiConsumer<Integer, Integer> onEntry) throws Exception {
        JsonElement curve = file.getAsJsonObject().get("curve");
        for (JsonElement he : curve.getAsJsonArray()) {
            JsonObject obj = he.getAsJsonObject();
            onEntry.accept(obj.get("x").getAsInt(), obj.get("y").getAsInt());
        }
    }

    static final class HealthArmor {
        int health;
        int dr;
        int er;
        int rr;
    }
}
