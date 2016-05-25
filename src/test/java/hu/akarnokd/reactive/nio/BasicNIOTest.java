package hu.akarnokd.reactive.nio;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.junit.Test;

public class BasicNIOTest {

    @Test
    public void readFile() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("files/words.shakespeare.txt", "r")) {
            
            FileChannel fc = raf.getChannel();
            
            ByteBuffer buf = ByteBuffer.allocate(128);
            
            int read = fc.read(buf);
            
            while (read != -1) {
                System.out.println("Read: " + read);
                
                for (int i = 0; i < read; i++) {
                    System.out.print((char)buf.get(i));
                }
                
                buf.clear();
                
                read = fc.read(buf);
            }
        }
        
        System.out.println();
    }
    
    @Test
    public void selector() throws IOException {
        
        ByteBuffer getIndex = ByteBuffer.wrap("GET / HTTP/1.0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        ByteBuffer result = ByteBuffer.allocate(32);
        
        try (SocketChannel socket = SocketChannel.open()) {
            socket.connect(new InetSocketAddress("localhost", 8080));

            socket.configureBlocking(false);

            Selector selector = Selector.open();

            socket.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            
            while (selector.isOpen()) {
                selector.select();
                
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                
                while (it.hasNext()) {
                    SelectionKey k = it.next();
                    
                    if (k.isWritable()) {
                        socket.write(getIndex);
                    }
                    if (k.isReadable()) {
                        if (socket.read(result) >= 0) {
                            result.flip();
                            
                            while (result.hasRemaining()) {
                                System.out.print((char)result.get());
                            }
                            
                            result.clear();
                        } else {
                            selector.close();
                        }
                    }
                    
                    it.remove();
                }
            }
        }
    }
}
