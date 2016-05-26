package hu.akarnokd.reactive.ipc;

import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;

import org.junit.Test;

import hu.akarnokd.rxjava2.disposables.Disposable;
import hu.akarnokd.rxjava2.internal.schedulers.SingleScheduler;

public class FileWatcherTest {
    @Test(timeout = 10000)
    public void changeNotification() throws Exception {
        SingleScheduler single = new SingleScheduler();
        Path p = Paths.get("test.dat");
        Path d = Paths.get(".");
        
        try (RandomAccessFile write = new RandomAccessFile("test.dat", "rw");
                RandomAccessFile read = new RandomAccessFile("test.dat", "r")) {
            
            WatchService watcher = FileSystems.getDefault().newWatchService();
            
            d.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            
            Disposable c = single.scheduleDirect(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        WatchKey k = watcher.take();
                        k.pollEvents().forEach(System.out::println);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            });
            
            Thread.sleep(1000);
            
            write.write(new byte[1024]);
            write.getFD().sync();

            Thread.sleep(1000);

            write.seek(512);
            write.write(new byte[128]);
            Files.setLastModifiedTime(p, FileTime.fromMillis(System.currentTimeMillis()));
            write.getFD().sync();

            Thread.sleep(1000);
            c.dispose();
        } finally {
            Files.deleteIfExists(p);
        }
        
    }
}
