package hu.akarnokd.fallout76;

import java.io.*;
import java.nio.ByteBuffer;

public class DataInputByteBuffer implements DataInput {

    private final ByteBuffer buffer;

    public DataInputByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public long position() {
        return buffer.position();
    }

    public long length() {
        return buffer.capacity();
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (buffer.remaining() < len) {
            throw new EOFException("Could not read " + len + " bytes as only " + buffer.remaining() + " bytes are available.");
        }
        buffer.get(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int r = buffer.remaining();
        int p = buffer.position();
        if (r < n) {
            buffer.position(p + r);
            return r;
        }
        buffer.position(p + n);
        return n;
    }

    RuntimeException unsupported() {
        return new UnsupportedOperationException("Not all read operations are supported");
    }

    @Override
    public boolean readBoolean() throws IOException {
        throw unsupported();
    }

    @Override
    public byte readByte() throws IOException {
        return buffer.get();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return buffer.get() & 0xFF;
    }

    @Override
    public short readShort() throws IOException {
        throw unsupported();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        throw unsupported();
    }

    @Override
    public char readChar() throws IOException {
        throw unsupported();
    }

    @Override
    public int readInt() throws IOException {
        return buffer.getInt();
    }

    @Override
    public long readLong() throws IOException {
        throw unsupported();
    }

    @Override
    public float readFloat() throws IOException {
        throw unsupported();
    }

    @Override
    public double readDouble() throws IOException {
        throw unsupported();
    }

    @Override
    public String readLine() throws IOException {
        throw unsupported();
    }

    @Override
    public String readUTF() throws IOException {
        throw unsupported();
    }
}
