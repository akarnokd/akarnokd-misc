package hu.akarnokd.reactor;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.junit.*;

import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;

public class PublishOnCheck {
    static class GroupsAndDelays {
        int groupMemberDelaySeconds;
        int maxRetries;
        int retryDelaySeconds;

        public Flux<Integer> doStuff(Flux<Integer> src,
                                     Function<Integer, Integer> groupByFn,
                                     Function<Integer, Integer> responseMapper) {
            return src
                    .groupBy(groupByFn)
                    .flatMap(g -> g
                            .distinct()
                            .collectList()
                            //.publishOn(Schedulers.newParallel("par-grp"))
                            .flatMap(this::call))
                    .doOnNext(i -> {
                        System.out.printf("Received: %d.%n", i);
                    });
        }

        private Flux<Integer> call(List<Integer> values) {
            return Flux.fromIterable(values)
                    .zipWith(Flux.intervalMillis(1000 * groupMemberDelaySeconds, Schedulers.newTimer("par-grp")),
                            (x, delay) -> x)
                    .flatMap(this::even);
        }

        private Mono<Integer> even(int i) {
            return Mono.<Integer>create(emitter -> {
                if (i > 30) {
                    System.out.printf("Too big: %d.%s%n", i, Thread.currentThread().getName());
                    emitter.error(new IllegalArgumentException("Too big."));
                } else if (i % 2 == 0) {
                    System.out.printf("Success: %d.%s%n", i, Thread.currentThread().getName());
                    emitter.success(i);
                } else {
                    emitter.success();
                }
            })
                    .map(x -> x * 2)
                    .retryWhen(errors -> errors.zipWith(Flux.range(1, maxRetries + 1), (ex, x) -> x)
                            .flatMap(retryCount -> {
                                if (retryCount <= maxRetries) {
                                    long delay = (long) Math.pow(retryDelaySeconds, retryCount);
                                    return Mono.delay(Duration.ofSeconds(delay));
                                }
                                System.out.printf("Done retrying: %d.%n", i);
                                return Mono.<Long>empty();
                            })
                    );
        }
    }
    
    @Test
    public void test() {
        final int groupMemberDelaySeconds = 3;
                final int maxRetryCount = 2;
                final int retryDelaySeconds = 2;
                Function<Integer, Integer> groupByFn;
                Function<Integer, Integer> responseMapper;

                GroupsAndDelays groupsAndDelays;

                final Flux<Integer> src = Flux.fromArray(new Integer[] {
                        1, 2, 3, 4, 5, 1, 2, 3, 4, 5,
                        11, 12, 13, 14, 15, 11, 12, 13, 14, 15,
                        21, 22, 23, 24, 25, 21, 22, 23, 24, 25,
                        31, 32, 33, 34, 35, 31, 32, 33, 34, 35,
                        41, 42, 43, 44, 45, 41, 42, 43, 44, 45
                });
                
                groupsAndDelays = new GroupsAndDelays();
                        groupsAndDelays.groupMemberDelaySeconds = groupMemberDelaySeconds;
                        groupsAndDelays.maxRetries = maxRetryCount;
                        groupsAndDelays.retryDelaySeconds = retryDelaySeconds;

                        groupByFn = i -> i % 10;
                        responseMapper = i -> i;
                        
                groupsAndDelays.doStuff(src, groupByFn, responseMapper)
                        .collectList()
                        .block();
    }
}
