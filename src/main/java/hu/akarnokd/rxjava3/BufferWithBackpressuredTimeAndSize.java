package hu.akarnokd.rxjava3;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BufferWithBackpressuredTimeAndSize {

    public static void main(String[] args) {
        var src = Flowable.concatArray(
                Flowable.range(1, 19),
                Flowable.empty().delay(5, TimeUnit.SECONDS),
                Flowable.range(21, 40)
            )
              ;
        bufferWithTime(src, 5, 1, TimeUnit.SECONDS, Schedulers.computation())
        .blockingSubscribe(System.out::println);
    }

    static <T> Flowable<List<T>> bufferWithTime(Flowable<T> source, int maxSize, long time, TimeUnit unit, Scheduler scheduler) {
        return Flowable.defer(() -> {
            AtomicReference<List<T>> buffer = new AtomicReference<List<T>>(new ArrayList<T>(maxSize));
            PublishProcessor<Object> STOP = PublishProcessor.create();
            Notification<List<T>> TIMETRIGGER = Notification.createOnNext(new ArrayList<T>());
            BehaviorProcessor<Notification<List<T>>> TIMER = BehaviorProcessor.createDefault(TIMETRIGGER);

            return Flowable.mergeArray(
                    3, maxSize,
                    source.materialize(),
                    TIMER.switchMap(v -> Flowable.interval(time, unit, scheduler).map(w -> v))
                    .takeUntil(STOP)
                )
            .flatMap(evt -> {
                if (evt == TIMETRIGGER) {
                    List<T> buf = buffer.getAndSet(new ArrayList<T>(maxSize));
                    return Flowable.just(buf);
                }
                if (evt.isOnError()) {
                    List<T> buf = buffer.getAndSet(null);
                    STOP.onNext(TIMETRIGGER);
                    return Flowable.just(buf).concatWith(
                            Flowable.<List<T>>error(evt.getError()));
                }
                if (evt.isOnComplete()) {
                    List<T> buf = buffer.getAndSet(null);
                    STOP.onNext(TIMETRIGGER);
                    return Flowable.just(buf);
                }
                TIMER.onNext(TIMETRIGGER);
                List<T> buf = buffer.get();
                @SuppressWarnings("unchecked")
                T v = (T)evt.getValue();
                buf.add(v);
                if (buf.size() == maxSize) {
                    buffer.set(new ArrayList<T>(maxSize));
                    return Flowable.just(buf);
                }
                return Flowable.<List<T>>empty();
            });
        });
    }
}
