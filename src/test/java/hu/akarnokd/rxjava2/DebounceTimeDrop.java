package hu.akarnokd.rxjava2;

    import java.util.Queue;
    import java.util.concurrent.*;
    import java.util.concurrent.atomic.AtomicLong;
    
    import org.junit.Test;
    
    import io.reactivex.*;
    import io.reactivex.functions.Consumer;
    import io.reactivex.schedulers.*;
    import io.reactivex.subjects.PublishSubject;
    
    public class DebounceTimeDrop {
    
        @Test
        public void test() {
            PublishSubject<Integer> source = PublishSubject.create();
            
            TestScheduler scheduler = new TestScheduler();
            
            source.compose(debounceTime(10, TimeUnit.MILLISECONDS, scheduler, v -> {
                System.out.println(
                        "Dropped: " + v + " @ T=" + scheduler.now(TimeUnit.MILLISECONDS));
            }))
            .subscribe(v -> System.out.println(
                    "Passed: " + v + " @ T=" + scheduler.now(TimeUnit.MILLISECONDS)),
                    Throwable::printStackTrace, 
                    () -> System.out.println(
                            "Done "  + " @ T=" + scheduler.now(TimeUnit.MILLISECONDS)));
            
            source.onNext(1);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
    
            scheduler.advanceTimeBy(20, TimeUnit.MILLISECONDS);
    
            source.onNext(2);
            scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
            source.onNext(4);
            scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
            source.onNext(5);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
    
            scheduler.advanceTimeBy(20, TimeUnit.MILLISECONDS);
    
            source.onNext(6);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
    
            scheduler.advanceTimeBy(20, TimeUnit.MILLISECONDS);
            
            source.onComplete();
        }
        
        public static <T> ObservableTransformer<T, T> debounceTime(
                long time, TimeUnit unit, Scheduler scheduler, 
                Consumer<? super T> dropped) {
            return o -> Observable.<T>defer(() -> {
                AtomicLong index = new AtomicLong();
                Queue<Timed<T>> queue = new ConcurrentLinkedQueue<>();
                
                return o.map(v -> {
                    Timed<T> t = new Timed<>(v, index.getAndIncrement(), TimeUnit.NANOSECONDS);
                    queue.offer(t);
                    return t;
                })
                .debounce(time, unit, scheduler)
                .map(v -> {
                    while (!queue.isEmpty()) {
                        Timed<T> t = queue.peek();
                        if (t.time() < v.time()) {
                            queue.poll();
                            dropped.accept(t.value());
                        } else
                        if (t == v) {
                            queue.poll();
                            break;
                        }
                    }
                    return v.value();
                })
                .doOnComplete(() -> {
                    while (!queue.isEmpty()) {
                        dropped.accept(queue.poll().value());
                    }
                });
            });
        }
    }
