package hu.akarnokd.rxjava2;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.common.base.Charsets;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class FlowableStringInputStreamTest {

    static Charset utf8 = Charsets.UTF_8;

    @Test
    public void simple() throws Exception {

        InputStream is = FlowableStringInputStream.createInputStream(Flowable.just("abc", "def", "ghi", "jkl", "mno"), utf8);
        byte[] buf = new byte[4];

        assertEquals(3, is.available());

        assertEquals('a', is.read());

        assertEquals(2, is.available());

        assertEquals(2, is.read(buf));

        assertEquals('b', buf[0]);
        assertEquals('c', buf[1]);

        assertEquals(1, is.read(buf, 2, 1));

        assertEquals('d', buf[2]);

        assertEquals(1, is.read(buf, 3, 1));

        assertEquals('e', buf[3]);

        assertEquals(1, is.read(buf));

        assertEquals('f', buf[0]);

        buf = new byte[9];

        DataInputStream bin = new DataInputStream(is);

        bin.readFully(buf, 0, 9);

        assertArrayEquals("ghijklmno".getBytes(utf8), buf);

        assertEquals(-1, is.read());

        assertEquals(-1, is.read(buf));
        
        assertEquals(0, is.available());
    }

    @Test
    public void error() throws IOException{
        Flowable<String> f = Flowable.error(new IllegalArgumentException());

        InputStream is = FlowableStringInputStream.createInputStream(f, utf8);

        try {
            is.read();
            fail("Should have thrown");
        } catch (IOException ex) {
            if (!(ex.getCause() instanceof IllegalArgumentException)) {
                throw ex;
            }
        }

        try {
            is.read(new byte[1]);
            fail("Should have thrown");
        } catch (IOException ex) {
            if (!(ex.getCause() instanceof IllegalArgumentException)) {
                throw ex;
            }
        }

        assertEquals(0, is.available());
    }

    @Test
    public void error2() throws IOException{
        Flowable<String> f = Flowable.error(new IOException("expect"));

        InputStream is = FlowableStringInputStream.createInputStream(f, utf8);

        try {
            is.read();
            fail("Should have thrown");
        } catch (IOException expected) {
            assertEquals("expect", expected.getMessage());
        }

        try {
            is.read(new byte[1]);
            fail("Should have thrown");
        } catch (IOException expected) {
            assertEquals("expect", expected.getMessage());
        }

        assertEquals(0, is.available());
    }

    @Test(timeout = 10000)
    public void async() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        Flowable<String> f = Flowable.range(100, 10).map(Object::toString)
                .doOnCancel(() -> calls.incrementAndGet())
                .subscribeOn(Schedulers.computation())
                .delay(10, TimeUnit.MILLISECONDS);
        
        try (InputStream is = FlowableStringInputStream.createInputStream(f, utf8)) {
            assertEquals('1', is.read());
            assertEquals('0', is.read());
            assertEquals('0', is.read());
            
            byte[] buf = new byte[3];
            assertEquals(3, is.read(buf));

            assertArrayEquals("101".getBytes(utf8), buf);
        }

        assertEquals(1, calls.get());
    }

    @Test(timeout = 10000)
    public void asyncCancel() throws Exception {
        InputStream is = FlowableStringInputStream.createInputStream(Flowable.never(), utf8);
        
        Schedulers.single().scheduleDirect(() -> {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 500, TimeUnit.MILLISECONDS);

        assertEquals(-1, is.read());
    }

    @Test
    public void interruptAsync() {
        InputStream is = FlowableStringInputStream.createInputStream(Flowable.never(), utf8);

        Thread t = Thread.currentThread();
        try {
            Schedulers.single().scheduleDirect(() -> {
                t.interrupt();
            }, 500, TimeUnit.MILLISECONDS);
            
            try {
                is.read();
                fail("Should have thrown");
            } catch (InterruptedIOException expected) {
            } catch (IOException ex) {
                throw new AssertionError(ex.toString(), ex);
            }
        } finally {
            Thread.interrupted();
        }
    }
    
    @Test
    public void indexVerify() throws IOException {
        InputStream is = FlowableStringInputStream.createInputStream(Flowable.just("abc"), utf8);

        is.read(new byte[2], 0, 0);

        try {
            is.read(new byte[2], -1, 1);
            fail("Should have thrown");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            is.read(new byte[2], 1, -1);
            fail("Should have thrown");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            is.read(new byte[2], 3, 1);
            fail("Should have thrown");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            is.read(new byte[2], 1, 2);
            fail("Should have thrown");
        } catch (IndexOutOfBoundsException expected) {
        }
    }
}
