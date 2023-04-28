package hu.akarnokd.imperium;

import java.io.*;
import java.nio.*;
import java.nio.file.*;

public class XMFExplorer {

    static int threeBytes(ByteBuffer bb, int offset) {
        return (bb.get(offset) & 0xFF)
                + (bb.get(offset + 1) & 0xFF) * 256
                + (bb.get(offset + 2) & 0xFF) * 256 * 256;
    }
    
    public static void main(String[] args) throws Exception {
        
        var basePath = "c:\\Games\\Imperium Galactica GoG English\\MUSIC\\";
        var fileName = "MAIN1.XMF";
        var bytes = Files.readAllBytes(Paths.get(basePath + fileName));
        
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        
        int offset = 0;
        
        int i = 0;
        while (bb.get(offset) != 0) {
            int offsetA = threeBytes(bb, offset + 4);
            int offsetB = threeBytes(bb, offset + 7);
            int offsetC = threeBytes(bb, offset + 10);
            System.out.printf("%02d, Flags: %02X, V1: %06X, V2: %06X, V3: %06X, V4: %06X, Diff: %06X%n", i, 
                    bb.get(offset), threeBytes(bb, offset + 1), offsetA, offsetB, 
                    offsetC, offsetC - offsetB);
            
            i++;
            offset += 16;
        }
        
        int offset0 = 0x3A40D;
        i = 1;
        
        while (offset0 < bytes.length) {
        
            int len1 = bb.getShort(offset0) & 0xFFFF;
        
            System.out.printf("[%8X]: %4X (%d)%n", offset0, len1, len1);

            /*
            if (offset0 + len1 >= bytes.length) {
                len1 = bytes.length - offset0;
            }
            byte[] copy = new byte[len1];
            System.arraycopy(bytes, offset0, copy, 0, len1);
            
            WavExport.writeWav(new File(basePath + fileName + "_" + "%02d.wav".formatted(i)), copy);
            */
            
            offset0 += len1;
            i++;
        }
    }
    

}
