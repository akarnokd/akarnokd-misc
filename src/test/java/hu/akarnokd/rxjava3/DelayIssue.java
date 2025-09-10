package hu.akarnokd.rxjava3;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.internal.disposables.SequentialDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DelayIssue {

    public static void main(String[] args) {
        for (int attempt = 1; attempt <= 1000; attempt++) {
            ConcurrentLinkedDeque<Integer> sink = new ConcurrentLinkedDeque<>();
            SequentialDisposable disposable = new SequentialDisposable();
            AtomicInteger last = new AtomicInteger(-1);
            disposable.replace(Observable.range(0, 10)
                    .delay(1, TimeUnit.MICROSECONDS, Schedulers.computation(), true)
                    .onTerminateDetach()
                    .doOnNext(value -> {
                        if (!disposable.isDisposed()) {
                            Schedulers.computation().scheduleDirect(disposable::dispose);
                        }
                        sink.add(value);
                        if (last.getAndSet(value) != value - 1) {
                            System.err.println("Out of order!!!");
                        }
                    })
                    .subscribe());
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
            List<Integer> values = List.copyOf(sink);
            List<String> errors = new ArrayList<>();
            int expected = 0;
            for (int i = 0; i < values.size(); i++) {
                int value = values.get(i);
                if (value != expected) {
                    errors.add((errors.size() + 1) + ". Wrong value at index " + i + " (expected " + expected +
                            ", was " + value + ")");
                }
                expected = value + 1;
            }
            if (!errors.isEmpty()) {
                throw new AssertionError(new StringBuilder()
                        .append("Attempt ")
                        .append(attempt)
                        .append(" failed.\n\t\tError(s):\n\t\t\t")
                        .append(String.join("\n\t\t\t", errors))
                        .append("\n\t\tValues: ")
                        .append(values)
                        .toString());
            }
        }
    }
}
