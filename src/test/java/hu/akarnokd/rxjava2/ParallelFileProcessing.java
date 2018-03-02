package hu.akarnokd.rxjava2;

import java.nio.file.*;
import java.util.stream.Stream;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.parallel.ParallelFlowable;
import io.reactivex.schedulers.Schedulers;

public class ParallelFileProcessing {

    @Test
    public void test() {
        ParallelFlowable<Path> pf = 
                Flowable.<Path, Stream<Path>>using(
                    () -> Files.list(Paths.get("/my/dir/with/files")),
                    files -> Flowable.fromIterable((Iterable<Path>)() -> files.iterator()),
                    AutoCloseable::close
                )
                .parallel(2)
                .runOn(Schedulers.computation())
                .filter(Files::isRegularFile);
    }
}
