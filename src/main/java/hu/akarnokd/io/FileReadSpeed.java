package hu.akarnokd.io;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class FileReadSpeed {

    public static void main(String[] args) throws Throwable {
        
        Files.list(Paths.get("c:\\Program Files (x86)\\Steam\\steamapps\\common\\Fallout76\\Data\\"))
        .forEach(f -> {
            byte[] buffer = new byte[1024 * 1024 * 128];
            
            if (!Files.isDirectory(f)) {
                
                try (InputStream in = new FileInputStream(f.toFile())) {
                    System.out.printf("%s, , size: %s MB, age: %s", f.getFileName(),
                            Files.size(f) / 1024 / 1024,
                            ChronoUnit.DAYS.between(Clock.systemUTC().instant(), Files.getLastModifiedTime(f).toInstant())
                            );

                    long t0 = System.nanoTime();
                    for (;;) {
                        int r = in.read(buffer);
                        if (r < 0) {
                            break;
                        }
                    }
                    double t1 = (System.nanoTime() - t0) / 1000d / 1000d /1000d;
                    
                    System.out.printf(", speed: %.2f MB/s%n", 
                            Files.size(f) / 1024d / 1024d / t1
                            );
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                
            }
        });
    }
}
