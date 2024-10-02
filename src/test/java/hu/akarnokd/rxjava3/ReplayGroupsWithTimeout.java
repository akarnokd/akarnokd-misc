package hu.akarnokd.rxjava3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import io.reactivex.rxjava3.core.Emitter;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class ReplayGroupsWithTimeout {

record MessageValue<T>(T value) { }
record MessageSubscriber<T>(Emitter<T> emitter) { }
record MessageDisposed<T>(Emitter<T> emitter) { }
record MessageTimeout<K>(K key, long id) { }
record CachedValue<T>(T value, long id, Disposable timeout) { }

    @Test
    public void test() {
var subject = PublishSubject.<String>create();
var sched = new TestScheduler();
var output = groupCacheLatestWithTimeout(subject, 
        5, TimeUnit.SECONDS, sched, 
        k -> k.substring(0, 2));

var to1 = output.test();

to1.assertEmpty();

subject.onNext("g1-1");

to1.assertValuesOnly("g1-1");

subject.onNext("g1-2");

to1.assertValuesOnly("g1-1", "g1-2");

var to2 = output.test();

to2.assertValuesOnly("g1-2");

sched.advanceTimeBy(10, TimeUnit.SECONDS);

to1.assertValuesOnly("g1-1", "g1-2");
to2.assertValuesOnly("g1-2");

var to3 = output.test();

to3.assertEmpty();

subject.onNext("g1-3");

to1.assertValuesOnly("g1-1", "g1-2", "g1-3");
to2.assertValuesOnly("g1-2", "g1-3");
to3.assertValuesOnly("g1-3");

to1.dispose();

subject.onNext("g1-4");

to1.assertValuesOnly("g1-1", "g1-2", "g1-3");
to2.assertValuesOnly("g1-2", "g1-3", "g1-4");
to3.assertValuesOnly("g1-3", "g1-4");
    }
    
public static <T, K> Observable<T> groupCacheLatestWithTimeout(
        Observable<T> source, 
        long timeout, TimeUnit unit, Scheduler scheduler,
        Function<T, K> keySelector) {
    Subject<Object> messageQueue = PublishSubject.create().toSerialized(); 
    
    Map<K, CachedValue<T>> cache = new LinkedHashMap<>();
    List<Emitter<T>> emitters = new ArrayList<>();
    
    AtomicLong idGenerator = new AtomicLong();

    var result = Observable.<T>create(emitter -> {
        messageQueue.onNext(new MessageSubscriber<T>(emitter));
        emitter.setCancellable(() -> {
            messageQueue.onNext(new MessageDisposed<T>(emitter));
        });
    });
    
    messageQueue.subscribe(message -> {
        if (message instanceof MessageValue) {
            var mv = ((MessageValue<T>)message).value;
            var key = keySelector.apply(mv);
            
            var old = cache.get(key);
            if (old != null) {
                old.timeout.dispose();
            }
            
            var id = idGenerator.incrementAndGet();
            
            var dispose = scheduler.scheduleDirect(() -> {
                messageQueue.onNext(new MessageTimeout<K>(key, id));
            });
            
            cache.put(key, new CachedValue<T>(mv, id, dispose));
            
            for (var emitter : emitters) {
                emitter.onNext(mv);
            }
        }
        else if (message instanceof MessageSubscriber) {
            
            var me = ((MessageSubscriber<T>)message).emitter;
            emitters.add(me);
            
            for (var entry : cache.values()) {
                me.onNext(entry.value);
            }
        }
        else if (message instanceof MessageDisposed) {
            var md = ((MessageDisposed<T>)message).emitter;
            emitters.remove(md);
        }
        else if (message instanceof MessageTimeout) {
            var mt = ((MessageTimeout<K>)message);
            
            var entry = cache.get(mt.key);
            if (entry.id == mt.id) {
                cache.remove(mt.key);
            }
        }
    });
    
    source.subscribe(value -> {
        messageQueue.onNext(new MessageValue<>(value));
    });

    return result;
}
}
