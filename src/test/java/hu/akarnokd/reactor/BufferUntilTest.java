package hu.akarnokd.reactor;

import java.time.Duration;

import org.junit.Test;

import reactor.core.publisher.*;

public class BufferUntilTest {

    @Test
    public void test() {
        Hooks.onOperatorDebug();

        Flux.range(0, 1000)
        .bufferUntil(v -> true)
        .flatMap(v -> Mono.delay(Duration.ofMillis(10)))
        .blockLast();
    }
}
