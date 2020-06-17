package hu.akarnokd.fallout76;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * Processes a byte array of Elder Scrolls {@code .strings} file.
 */
public final class TesStringsData {

    private TesStringsData() {
        throw new IllegalStateException("No instances!");
    }

    public static void process(byte[] data, BiConsumer<Integer, String> onEntry) {
        int count = getInt32(data, 0);
        int headerSize = 8 + count * 8;

        for (int i = 0; i < count; i++) {

            int id = getInt32(data, 8 + 8 * i);
            int offset = getInt32(data, 12 + 8 * i);

            String string = getZString(data, headerSize + offset);

            onEntry.accept(id, string);
        }
    }

    static int getInt32(byte[] data, int offset) {
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    static String getZString(byte[] data, int offset) {
        int end = offset;
        while (data[end] != 0 && end < data.length) {
            end++;
        }
        byte[] copy = Arrays.copyOfRange(data, offset, end);
        return new String(copy, StandardCharsets.UTF_8);
    }
}
