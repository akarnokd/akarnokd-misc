package hu.akarnokd.reactor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import io.reactivex.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class MonoTimeoutTest {

    @Test
    public void test1() {
        String result = Mono.fromCallable(() -> {

            try {
                TimeUnit.SECONDS.sleep(10L);
            } catch (InterruptedException ignore) {
            }
            return "result from plan A";
            })
            .doOnCancel(() -> System.out.println("Cancel"))
            .timeout(Duration.ofSeconds(1L))
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(t -> Mono.fromCallable(() -> {
                System.out.println("plan B running on thread:" + Thread.currentThread().getName());
                return "result from plan B";
            }))
            .block();

        Assert.assertEquals("result from plan B", result);
    }

    @Test
    public void test2() {
        String result = Mono.fromCallable(() -> {

            try {
                TimeUnit.SECONDS.sleep(10L);
            } catch (InterruptedException ignore) {
                ignore.printStackTrace();
            }
            return "result from plan A";
        })
        .doOnCancel(() -> {
            System.out.println("Cancel");
        })
        .timeout(Duration.ofSeconds(1L))
        .onErrorResume(t -> Mono.fromCallable(() -> {
            System.out.println("plan B running on thread:" + Thread.currentThread().getName());
            return "result from plan B";
        }))
        .subscribeOn(Schedulers.single())
        .block();

        Assert.assertEquals("result from plan B", result);
    }

    @Test
    public void test3() {
        String result = Flowable.fromCallable(() -> {

            try {
                TimeUnit.SECONDS.sleep(10L);
            } catch (InterruptedException ignore) {
            }
            return "result from plan A";
            })
            .timeout(1, TimeUnit.SECONDS)
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .onErrorResumeNext((Throwable t) -> Mono.fromCallable(() -> {
                System.out.println("plan B running on thread:" + Thread.currentThread().getName());
                return "result from plan B";
            }))
            .blockingFirst();

        Assert.assertEquals("result from plan B", result);
    }

    @Test
    public void test4() {
        String result = Flowable.fromCallable(() -> {

            try {
                TimeUnit.SECONDS.sleep(10L);
            } catch (InterruptedException ignore) { }
            return "result from plan A";
        })
        .timeout(1, TimeUnit.SECONDS)
        .onErrorResumeNext((Throwable t) -> Mono.fromCallable(() -> {
            System.out.println("plan B running on thread:" + Thread.currentThread().getName());
            return "result from plan B";
        }))
        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
        .blockingFirst();

        Assert.assertEquals("result from plan B", result);
    }

    @Test
    public void test5() {
        String result = Single.fromCallable(() -> {

            try {
                TimeUnit.SECONDS.sleep(10L);
            } catch (InterruptedException ignore) { }
            return "result from plan A";
        })
        .timeout(1, TimeUnit.SECONDS)
        .onErrorResumeNext((Throwable t) -> Single.fromCallable(() -> {
            System.out.println("plan B running on thread:" + Thread.currentThread().getName());
            return "result from plan B";
        }))
        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
        .blockingGet();

        Assert.assertEquals("result from plan B", result);
    }
}
