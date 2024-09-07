package hu.akarnokd.theplanetcrafter;

import java.nio.file.*;

public class FileToByteArrayText {

    public static void main(String[] args) throws Throwable {
        
        var data = Files.readAllBytes(Paths.get("c:\\Users\\akarnokd\\Downloads\\back_circle_arrow.png"));
        
        int j = 0;
        for (var i : data) {
            System.out.printf("0x%02X", i);
            System.out.print(", ");
            
            if (++j == 16) {
                j = 0;
                System.out.println();
            }
        }
    }
}
