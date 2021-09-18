package hu.akarnokd.fallout76;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;

public class EditorIDScanner {

    public static void main(String[] args) throws Exception {
        File file = new File(
                "c:\\Program Files (x86)\\Bethesda.net Launcher\\games\\Fallout76\\Data\\SeventySix.esm");

        /*
         * char[4] = 'EDID'
         * uint16 = length
         * uint8[length] = characters
         */
        int i = 1;
        try (PrintWriter out = new PrintWriter(new FileWriter("editorids.txt"))) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

                MappedByteBuffer mbb = raf.getChannel().map(MapMode.READ_ONLY, 0, raf.length());


                while (mbb.remaining() > 0) {
                    if (mbb.get() == 'E') {
                        if (mbb.get() == 'D') {
                            if (mbb.get() == 'I') {
                                if (mbb.get() == 'D') {
                                    int len = Short.reverseBytes(mbb.getShort()) & 0xFFFF;
                                    byte[] data = new byte[len];
                                    mbb.get(data);
                                    out.println(new String(data, StandardCharsets.ISO_8859_1));
                                    if (i % 100 == 0) {
                                        System.out.println("Records so far: " + i);
                                    }
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
