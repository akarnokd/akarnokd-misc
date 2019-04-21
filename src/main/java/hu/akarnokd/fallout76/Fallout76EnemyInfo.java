package hu.akarnokd.fallout76;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.gson.*;

import io.reactivex.functions.BiConsumer;

public class Fallout76EnemyInfo {

    public static void main(String[] args) throws Exception {
        File file = new File(
                "c:\\Program Files (x86)\\Bethesda.net Launcher\\games\\Fallout76\\Data\\SeventySix - Startup.ba2");

        Ba2File ba2 = new Ba2File();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            ba2.read(raf);
            
            Map<String, JsonElement> armorJson = new HashMap<String, JsonElement>();
            Map<String, JsonElement> healthJson = new HashMap<String, JsonElement>();
            
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
                }
            }
            
            Map<String, Map<Integer, HealthArmor>> creatures = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : healthJson.entrySet()) {
               
                Map<Integer, HealthArmor> creature = creatures.computeIfAbsent(entry.getKey(), s -> new TreeMap<>());
                
                String name = entry.getKey();
                
                setCreatureEntry(entry.getValue(), (x, y) -> {
                    HealthArmor level = creature.computeIfAbsent(x, v -> new HealthArmor());
                    
                    level.health = y;
                });

                JsonElement drJson = armorJson.get(name + "_dr");
                if (drJson != null) {
                    setCreatureEntry(drJson, (x, y) -> {
                        HealthArmor level = creature.computeIfAbsent(x, v -> new HealthArmor());
                        
                        level.dr = y;
                    });
                }

                JsonElement erJson = armorJson.get(name + "_er");
                if (erJson != null) {
                    setCreatureEntry(erJson, (x, y) -> {
                        HealthArmor level = creature.computeIfAbsent(x, v -> new HealthArmor());
                        
                        level.er = y;
                    });
                }

                JsonElement rrJson = armorJson.get(name + "_rad");
                if (rrJson != null) {
                    setCreatureEntry(rrJson, (x, y) -> {
                        HealthArmor level = creature.computeIfAbsent(x, v -> new HealthArmor());
                        
                        level.rr = y;
                    });
                }
                
                System.out.println(name);
                
                for (Map.Entry<Integer, HealthArmor> e : creature.entrySet()) {
                    HealthArmor ha = e.getValue();
                    System.out.printf("    %3d:  %5d    %4d    %4d    %4d%n", e.getKey(), ha.health, ha.dr, ha.er, ha.rr);
                }
            }
        }
    }
    
    static void setCreatureEntry(JsonElement file, BiConsumer<Integer, Integer> onEntry) throws Exception {
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
