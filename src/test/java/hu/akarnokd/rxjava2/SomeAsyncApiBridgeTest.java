package hu.akarnokd.rxjava2;

    import java.util.concurrent.*;
    import java.util.function.Consumer;
    
    import org.junit.Test;
    
    public class SomeAsyncApiBridgeTest {
    
        static final class AsyncRange {
            final int max;
            int index;
            
            public AsyncRange(int start, int count) {
                this.index = start;
                this.max = start + count;
            }
    
            public CompletableFuture<Void> next(Consumer<? super Integer> consumer) {
                int i = index;
                if (i == max) {
                    return CompletableFuture.completedFuture(null);
                }
                index = i + 1;
                CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> consumer.accept(i));
                CompletableFuture<Void> cancel = new CompletableFuture<Void>() {
                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        cf.cancel(mayInterruptIfRunning);
                        return super.cancel(mayInterruptIfRunning);
                    }
                };
                return cancel;
            }
        }
    
        @Test
        public void simple() {
            AsyncRange r = new AsyncRange(1, 10);
            
            new SomeAsyncApiBridge<Integer>(
                    consumer -> r.next(consumer)
            )
            .test()
            .awaitDone(500, TimeUnit.SECONDS)
            .assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        }
    }
