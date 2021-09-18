package hu.akarnokd.rxjava2;

import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.Single;
import io.reactivex.subjects.SingleSubject;

public class ConcurrentSingleCache {
    static class Entity { }

    ConcurrentMap<String, SingleSubject<Entity>> map = new ConcurrentHashMap<>();

    Single<Entity> longLoadFromDatabase(String guid) {
        return Single.just(new Entity())
        .delay(1, TimeUnit.SECONDS);
    }

    public Single<Entity> getEntity(String guid) {
        SingleSubject<Entity> e = map.get(guid);
        if (e == null) {
            e = SingleSubject.create();
            SingleSubject<Entity> f = map.putIfAbsent(guid, e);
            if (f == null) {
                longLoadFromDatabase(guid).subscribe(e);
            } else {
                e = f;
            }
        }
        return e;
    }

    @Test
    public void test() throws Exception {
        String g = "Key";
        getEntity(g).subscribe(System.out::println);
        getEntity(g).subscribe(System.out::println);
        getEntity(g).subscribe(System.out::println);

        Thread.sleep(2000);
    }
}
