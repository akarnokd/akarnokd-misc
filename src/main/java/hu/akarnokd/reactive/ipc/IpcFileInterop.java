package hu.akarnokd.reactive.ipc;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.*;

import rx.internal.util.unsafe.UnsafeAccess;

public class IpcFileInterop implements Closeable {
    final FileChannel channel;
    
    final MappedByteBuffer buffer;
    
    final long address;
    
    final int size;
    
    public IpcFileInterop(String fileName, int size) throws IOException {
        this.channel = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE);
        this.buffer = channel.map(MapMode.READ_WRITE, 0, size);
        this.size = size;
        this.address = bufferAddress(buffer);
        if (address == 0) {
            throw new IllegalStateException("Unable to get a hold onto the buffer address.");
        }
    }
    
    public int size() {
        return size;
    }
    
    public int getAndAddInt(int offset, int value) {
        return UnsafeAccess.UNSAFE.getAndAddInt(null, address + offset, value);
    }
    
    public int addAndGetInt(int offset, int value) {
        return getAndAddInt(offset, value) + value;
    }

    public int getIntVolatile(int offset) {
        return UnsafeAccess.UNSAFE.getIntVolatile(null, address + offset);
    }
    
    public void setIntVolatile(int offset, int value) {
        UnsafeAccess.UNSAFE.putIntVolatile(null, address + offset, value);
    }
    
    public void set(int offset, byte b) {
        buffer.put(offset, b);
    }
    
    public byte get(int offset) {
        return buffer.get(offset);
    }
    
    public int getInt(int offset) {
        return buffer.getInt(offset);
    }
    
    public long getLong(int offset) {
        return buffer.getLong(offset);
    }
    
    public void setInt(int offset, int value) {
        buffer.putInt(offset, value);
    }

    public void setLong(int offset, long value) {
        buffer.putLong(offset, value);
    }

    public void set(int offset, byte[] b) {
        set(offset, b, 0, b.length);
    }

    public void set(int offset, byte[] b, int start, int count) {
        MappedByteBuffer buf = buffer;
        for (int i = 0; i < count; i++) {
            buf.put(offset + i, b[start + i]);
        }
    }

    
    static long bufferAddress(Buffer buf) {
        try {
            Field f = Buffer.class.getDeclaredField("address");
            f.setAccessible(true);
            return f.getLong(buf);
        } catch (Throwable ex) {
            return 0L;
        }
    }
    
    @Override
    public void close() throws java.io.IOException {
        channel.close();
    }
}
