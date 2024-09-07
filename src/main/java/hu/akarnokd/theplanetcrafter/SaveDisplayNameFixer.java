package hu.akarnokd.theplanetcrafter;

import java.nio.file.*;

public class SaveDisplayNameFixer {

    public static void main(String[] args) throws Throwable {
        var p = Paths.get("c:\\Users\\akarnokd\\Downloads\\Standard.json");
        var p2 = Paths.get("c:\\Users\\akarnokd\\Downloads\\Standard_fixed.json");
        var lines = Files.readAllLines(p);

        for (int i = 0; i < lines.size(); i++) {
            var s = lines.get(i);
            if (s.contains("saveDisplayName")) {
                s = s.replace("\\\\", "");
                lines.set(i, s);
            }
        }
        Files.write(p2, lines);
    }
}
