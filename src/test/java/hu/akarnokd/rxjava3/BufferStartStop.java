package hu.akarnokd.rxjava3;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.SerialDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class BufferStartStop {

    @Test
    public void test() {

        Observable.fromArray(1, 2, 3, 5, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31)
        .flatMap(v -> Observable.timer(v * 100, TimeUnit.MILLISECONDS).map(w -> v))
        .compose(BufferWithTimeout.create(700, TimeUnit.MILLISECONDS, Schedulers.computation()))
        .blockingSubscribe(System.out::println);
        
    }

public static final class BufferWithTimeout<T> {

    Scheduler.Worker trampoline = Schedulers.trampoline().createWorker();

    final long timeout;
    
    final TimeUnit unit;
    
    final Scheduler.Worker worker;
    
    final SerialDisposable timer = new SerialDisposable();
    
    final PublishSubject<List<T>> output = PublishSubject.create();
    
    List<T> current;
    
    long bufferIndex;
    
    BufferWithTimeout(long timeout, TimeUnit unit, Scheduler scheduler) {
        this.worker = scheduler.createWorker();
        this.timeout = timeout;
        this.unit = unit;
    }
    
    void onValue(T value) {
        trampoline.schedule(() -> {
            if (timer.isDisposed()) {
                return;
            }
            if (current == null) {
                current = new ArrayList<>();
                long bi = ++bufferIndex;
                timer.set(worker.schedulePeriodically(() -> {
                    onTime(bi);
                }, timeout, timeout, unit));
            }
            current.add(value);
        });
    }

    void onTime(long index) {
        trampoline.schedule(() -> {
            if (index == bufferIndex && current != null) {
                if (current.isEmpty()) {
                    current = null;
                    bufferIndex++;
                    timer.set(null);
                } else {
                    output.onNext(current);
                    current = new ArrayList<>();
                }
            }
        });
    }

    void onTerminate(Throwable error) {
        timer.dispose();
        worker.dispose();
        trampoline.schedule(() -> {
            if (current != null && !current.isEmpty()) {
                output.onNext(current);
                current = null;
            }
            if (error != null) {
                output.onError(error);
            } else {
                output.onComplete();
            }
        });
    }
    
    void dispose() {
        timer.dispose();
        worker.dispose();
        trampoline.schedule(() -> {
            current = null;
        });
    }

    public static <T> ObservableTransformer<T, List<T>> create(long timeout, TimeUnit unit, Scheduler scheduler) {
        return o -> 
            Observable.defer(() -> {
                BufferWithTimeout<T> state = new BufferWithTimeout<>(timeout, unit, scheduler);

                return  o
                        .doOnNext(v -> state.onValue(v))
                        .doOnError(e -> state.onTerminate(e))
                        .doOnComplete(() -> state.onTerminate(null))
                        .ignoreElements()
                        .<List<T>>toObservable()
                        .mergeWith(state.output.doOnDispose(state::dispose));
            });
    }
}
}
