package hu.akarnokd.rxjava2;

    import java.util.concurrent.TimeUnit;
    
    import io.reactivex.*;
    import io.reactivex.schedulers.TestScheduler;
    
    public class Coincidence {
    
        public static void main(String[] args) {
            TestScheduler sch = new TestScheduler();
            
            Flowable.interval(100, TimeUnit.MILLISECONDS, sch)
            .onBackpressureBuffer()
            .compose(coincide(Flowable.interval(300, TimeUnit.MILLISECONDS, sch), sch))
            .take(7)
            .subscribe(v -> System.out.printf("%d - %d%n", sch.now(TimeUnit.MILLISECONDS), v));
    
            sch.advanceTimeBy(1001, TimeUnit.MILLISECONDS);
        }
        
        static <T, U> FlowableTransformer<T, T> coincide(Flowable<U> other, Scheduler scheduler) {
            return f -> {
                return other.publish(g -> {
                    return f.flatMap(v -> {
                        return Flowable.just(v)
                                .delay(1, TimeUnit.MILLISECONDS, scheduler)
                                .takeUntil(g)
                        ;
                    }, 1);
                });
            };
        };
    }
