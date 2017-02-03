package hu.akarnokd.reactor;

    import java.time.Duration;
    import java.util.*;
    
    import reactor.core.publisher.*;
    
    public class DelayedBuffer {
    
        public static void main(String[] args) {
            Flux.just(1, 2, 3, 6, 7, 10)
            .flatMap(v -> Mono.delayMillis(v * 1000).doOnNext(w -> System.out.println("T=" + v)).map(w -> v))
            .compose(f -> delayedBufferAfterFirst(f, Duration.ofSeconds(2)))
            .doOnNext(System.out::println)
            .blockLast();
        }
        
        public static <T> Flux<List<T>> delayedBufferAfterFirst(Flux<T> source, Duration d) {
            return source
            .publish(f -> {
                return f.take(1).collectList()
                .concatWith(f.buffer(d).take(1))
                .repeatWhen(r -> r.takeUntilOther(f.ignoreElements()));
            });
        }
    }
