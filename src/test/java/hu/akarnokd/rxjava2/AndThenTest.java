package hu.akarnokd.rxjava2;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.*;

    public class AndThenTest {
    
        Integer value;
        
        @Test
        public void test() {
            getFromLocal()
            .switchIfEmpty(getFromNetwork()
                    .flatMapCompletable(v -> saveFoo(v))
                    .andThen(getFromLocal()))
            .doOnSuccess(e -> System.out.println("Success: " + e))
            .test()
            .awaitDone(5, TimeUnit.SECONDS)
            .assertResult(10);
        }
        
        Maybe<Integer> getFromLocal() {
            return Maybe.fromCallable(() -> {
                System.out.println("FromLocal called");
                return value;
            });
        }
        
        Single<Integer> getFromNetwork() {
            return Single.fromCallable(() -> {
                System.out.println("FromNetwork called");
                return 10;
            }).delay(100, TimeUnit.MILLISECONDS)
                    ;
        }
        
        Completable saveFoo(Integer v) {
            return Completable.fromRunnable(() -> {
                System.out.println("SaveFoo called");
                value = v;
            });
        }
    }
