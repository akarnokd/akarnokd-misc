package hu.akarnokd.theplanetcrafter;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class FusionReactorFix {

    public static void main(String[] args) throws Throwable {
        System.out.println("The Planet Crafter Fusion Reactor Inventory Fix");
        if (args.length == 0) {
            System.out.println("Usage:");
            System.out.println("FusionReactorFix filename.json");
            return;
        }
        
        var generators = new HashMap<String, String>();
        
        generators.put("109487734", "hill wreck");
        generators.put("102606011", "battleship");
        generators.put("101703877", "stargate");
        generators.put("101484917", "luxury cruiser");
        generators.put("107983344", "lava ship");

        
        var lines = Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8);
        
        var items = new HashMap<String, String>();
        
        var itempatter = Pattern.compile("\"id\":(\\d+),\"gId\":\"([a-zA-Z0-9_-]+)\"");
        var inventorypatter = Pattern.compile("\"id\":(\\d+),\"woIds\":\"([a-zA-Z0-9_,]+)\"");
        
        boolean save = false;
        
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            
            var m = itempatter.matcher(line);
            if (m.find()) {
                var id = m.group(1);
                var gid = m.group(2);
                
                items.put(id, gid);
            } else {
                var m2 = inventorypatter.matcher(line);
                
                if (m2.find()) {
                    var id = m2.group(1);
                    
                    var gen = generators.get(id);
                    if (gen != null) {
                        System.out.println("Generator: " + gen + " (" + id + "): " + m2.group(2));
                        
                        var woIds = new ArrayList<String>();
                        var changed = false;
                        
                        for (var woId : Arrays.asList(m2.group(2).split(","))) {
                            var gId = items.get(woId);
                            if (gId != null) {
                                if (!gId.equals("FusionEnergyCell")) {
                                    System.out.printf("  %s - %s -> removed%n", woId, gId);
                                    changed = true;
                                } else {
                                    woIds.add(woId);
                                }
                                
                            }
                        }
                        
                        if (changed) {
                            var woIds1 = m2.start(2);
                            var woIds2 = m2.end(2);
                            
                            System.out.println("  - " + line);
                            line = line.substring(0, woIds1) + String.join(",", woIds) + line.substring(woIds2);
                            System.out.println("  + " + line);
                            lines.set(i, line);
                            save = true;
                        } else {
                            System.out.println("  OK");
                        }
                    }
                }
            }
        }
        
        if (save) {
            System.out.println("Done. Saved in:");
            System.out.println(Paths.get(args[0] + "_fixed"));
            Files.write(Paths.get(args[0] + "_fixed"), lines, StandardCharsets.UTF_8);
        }
    }
}
