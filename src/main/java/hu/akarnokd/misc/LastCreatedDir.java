package hu.akarnokd.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

public class LastCreatedDir {

    record PathAndTime(Path path, FileTime time) { }
    
    public static void main(String[] args) throws Exception {
        
        Path path = Paths.get("..\\");
        List<PathAndTime> list = new ArrayList<>();
        
        try (var files = Files.list(path)) {
            files.forEach(p -> {
                if (Files.isDirectory(p)) {
                    try {
                        BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
                        FileTime fileTime = attr.creationTime();
                        list.add(new PathAndTime(p, fileTime));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        
        var result = list.stream().min((a, b) -> {
            return b.time.compareTo(a.time);
        });
        
        result.ifPresentOrElse(entry -> {
            System.out.println(entry.path + " @ " + entry.time);
        }, () -> {
            System.out.println("No directories found");
        });
        
        
    }
}
