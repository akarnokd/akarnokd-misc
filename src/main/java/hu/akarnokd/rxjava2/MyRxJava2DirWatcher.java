package hu.akarnokd.rxjava2;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

public class MyRxJava2DirWatcher {

    public Flowable<WatchEvent<?>> createFlowable(FileSystem fs, Path path) {

        return Flowable.create(subscriber -> {

            WatchService watcher = fs.newWatchService();
            
            subscriber.setCancellable(() -> watcher.close());
            
            boolean error = false;
            WatchKey key;
            try {

                key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            }
            catch (IOException e) {
                subscriber.onError(e);
                error = true;
            }

            while (!error) {
                key = watcher.take();

                for (final WatchEvent<?> event : key.pollEvents()) {
                    subscriber.onNext(event);
                }

                key.reset();
            }

        }, BackpressureStrategy.BUFFER);

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Path path = Paths.get("c:\\temp\\delete");
        final FileSystem fileSystem = path.getFileSystem();

        MyRxJava2DirWatcher my = new MyRxJava2DirWatcher();
        my.createFlowable(fileSystem, path).subscribeOn(Schedulers.computation()).subscribe(event -> {
            System.out.println("1>>Event kind:" + event.kind() + ". File affected: " + event.context() + ". "
                    + Thread.currentThread().getName());

        }, onError -> {
            System.out.println("1>>" + Thread.currentThread().getName());
            onError.printStackTrace();
        });

        // MyRxJava2DirWatcher my2 = new MyRxJava2DirWatcher();

        my.createFlowable(fileSystem, path).subscribeOn(Schedulers.computation()).subscribe(event -> {
            System.out.println("2>>Event kind:" + event.kind() + ". File affected: " + event.context() + ". "
                    + Thread.currentThread().getName());

        }, onError -> {
            System.out.println("2>>" + Thread.currentThread().getName());
            onError.printStackTrace();
        });

        TimeUnit.MINUTES.sleep(100000);

    }
}