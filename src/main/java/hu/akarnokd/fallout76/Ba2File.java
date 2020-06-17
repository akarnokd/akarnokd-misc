package hu.akarnokd.fallout76;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public final class Ba2File {

    public final List<Ba2FileEntry> entries = new ArrayList<>();

    public void open(File file, Predicate<String> readDataPredicate) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            read(raf, readDataPredicate);
        }
    }

    public void read(RandomAccessFile raf, Predicate<String> readDataPredicate) throws IOException {
        raf.seek(12);
        int numFiles = Integer.reverseBytes(raf.readInt());
        long nameTableOffset = Long.reverseBytes(raf.readLong());

        // get file entries

        for (int i = 0; i < numFiles; i++) {
            raf.seek(24L + i * 36 + 16);

            Ba2FileEntry entry = new Ba2FileEntry();
            entries.add(entry);

            entry.offset = Long.reverseBytes(raf.readLong());
            raf.readInt(); // non-real size?
            entry.size = Integer.reverseBytes(raf.readInt());
        }

        raf.seek(nameTableOffset);

        for (int i = 0; i < numFiles; i++) {
            Ba2FileEntry entry = entries.get(i);
            int nameLength = Short.reverseBytes(raf.readShort());
            byte[] name = new byte[nameLength];
            raf.read(name);

            entry.name = new String(name, StandardCharsets.ISO_8859_1);
        }

        for (Ba2FileEntry entry : entries) {
            if (readDataPredicate.test(entry.name)) {
                raf.seek(entry.offset);
                entry.data = new byte[entry.size];
                raf.readFully(entry.data);
            }
        }
    }
}
