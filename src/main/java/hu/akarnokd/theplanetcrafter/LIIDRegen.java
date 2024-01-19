package hu.akarnokd.theplanetcrafter;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class LIIDRegen {

    public static void main(String[] args) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\akarnokd\\Downloads\\Standard-1.json"));
        
        List<String> invs = new ArrayList<>();
        
        Pattern p = Pattern.compile("\"liId\":([1-9]\\d*),");
        Pattern p1 = Pattern.compile("\"id\":(10\\d*),");
        
        for (int i = lines.size() - 1; i >= 0; i--) {
            var s = lines.get(i);
            var m1 = p1.matcher(s);
            if (!m1.find()) {
                var m = p.matcher(s);
                if (m.find()) {
                    invs.add(String.format("{\"id\":%s,\"woIds\":\"\",\"size\":80}|%n", m.group(1)));
                }
            }
            else {
                lines.remove(i);
            }
        }
        
        Files.write(Paths.get("c:\\Users\\akarnokd\\Downloads\\Standard-1-restored.json"), lines);
        Collections.reverse(invs);
        invs.forEach(System.out::print);
    }
}
