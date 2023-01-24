package hu.akarnokd.planb;

import java.nio.file.*;
import java.util.stream.Stream;

public class PlanBCleanupCS {
    static void processFile(Path path) throws Throwable {
        var lines = Files.readAllLines(path);
        boolean modified = false;
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (line.trim().startsWith("// Token")) {
                lines.set(i, "");
                modified = true;
            }
            if (line.trim().startsWith("// (get) Token")) {
                lines.set(i, "");
                modified = true;
            }
            if (line.trim().startsWith("// (set) Token")) {
                lines.set(i, "");
                modified = true;
            }
            if (line.trim().startsWith("// (Invoke) Token")) {
                lines.set(i, "");
                modified = true;
            }
        }
        
        if (modified) {
            System.out.println("Updating " + path);
            Files.write(path, lines);
        }
    }
    
    static void processDirectory(Path path) throws Throwable {
        try (Stream<Path> ds = Files.list(path)) {
            ds.forEach(p -> {
                try {
                    if (Files.isDirectory(p)) {
                        processDirectory(p);
                    } else
                    if (p.getFileName().toString().toLowerCase().endsWith(".cs")) {
                        processFile(p);
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
    
    public static void main(String[] args) throws Throwable {
        processDirectory(Paths.get("c:\\temp\\planb\\"));
    }
}
