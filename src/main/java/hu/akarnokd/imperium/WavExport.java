package hu.akarnokd.imperium;

import java.io.*;

public class WavExport {
    /**
     * Writes the byte data as a wav file.
     * @param dst the destination file
     * @param sample the sample
     * @throws IOException on error
     */
    public static void writeWav(File dst, byte[] sample)
            throws IOException {
        try (DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dst), 1024 * 1024))) {
            writeWav(sample, dout);
        }
    }
    /**
     * Write the wav into an output stream.
     * @param sample the sample
     * @param dout the stream
     * @throws IOException on error
     */
    public static void writeWav(byte[] sample, DataOutputStream dout)
            throws IOException {
        int dataLen = sample.length + (sample.length % 2 == 0 ? 0 : 1);
        // HEADER
        dout.write("RIFF".getBytes("ISO-8859-1"));
        dout.writeInt(Integer.reverseBytes(36 + dataLen)); // chunk size
        dout.write("WAVE".getBytes("ISO-8859-1"));

        // FORMAT
        dout.write("fmt ".getBytes("ISO-8859-1"));
        dout.writeInt(Integer.reverseBytes(16)); // chunk size
        dout.writeShort(Short.reverseBytes((short)1)); // Format: PCM = 1
        dout.writeShort(Short.reverseBytes((short)1)); // Channels = 1
        dout.writeInt(Integer.reverseBytes(22050)); // Sample Rate = 22050
        dout.writeInt(Integer.reverseBytes(22050)); // Byte Rate = 22050
        dout.writeShort(Short.reverseBytes((short)1)); // Block alignment = 1
        dout.writeShort(Short.reverseBytes((short)8)); // Bytes per sample = 8

        // DATA
        dout.write("data".getBytes("ISO-8859-1"));
        dout.writeInt(Integer.reverseBytes(dataLen));
        for (byte aSample : sample) {
            dout.write(aSample + 128);
        }
        for (int i = sample.length; i < dataLen; i++) {
            dout.write(0x80);
        }
        dout.flush();
    }
}
