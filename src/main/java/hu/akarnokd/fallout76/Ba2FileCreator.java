package hu.akarnokd.fallout76;

import java.io.*;

import com.google.common.io.Files;

public class Ba2FileCreator {
    public static void main(String[] args) throws Exception {
        
        File f = new File("c:\\Users\\akarnokd\\Downloads\\fo76interface\\meatbagreplace.ba2");

        File includeFile = new File("c:\\Users\\akarnokd\\Downloads\\fo76interface\\streetlamp01.nif");
        String dirName = "meshes/setdressing";
        String fileName = "corpse_meatbag01.nif";
        String includeName = dirName + "/" + fileName;

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
            out.writeBytes("BTDX");
            out.writeInt(Integer.reverseBytes(1)); // version
            out.writeBytes("GNRL");
            out.writeInt(Integer.reverseBytes(1)); // number of files
            
            long includeLen = includeFile.length();
            out.writeLong(Long.reverseBytes(24 + 36 + includeLen));
            
            out.writeInt(Integer.reverseBytes(hash(fileName))); // Namehash ???
            out.writeBytes("nif");
            out.writeByte(0); // Extension zero
            out.writeInt(Integer.reverseBytes(hash(dirName))); // dir hash  ???
            out.writeInt(Integer.reverseBytes(0x0010_0100)); // flags ???
            out.writeLong(Long.reverseBytes(24 + 36)); // offset
            out.writeInt(Integer.reverseBytes(0)); // size if compressed
            out.writeInt(Integer.reverseBytes((int)includeLen)); // realsize
            out.writeInt(Integer.reverseBytes(0xBAADF00D)); // align/magic
            
            Files.copy(includeFile, out);
            
            out.writeShort(Short.reverseBytes((short)includeName.length()));
            out.writeBytes(includeName);
        }
    }
    
    static int hash(String str) {
        return 0;
    }
}
