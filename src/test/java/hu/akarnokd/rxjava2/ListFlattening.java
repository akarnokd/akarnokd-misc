package hu.akarnokd.rxjava2;

import java.util.ArrayList;

import org.junit.Test;

import rx.Observable;

public class ListFlattening {

    static class User { }

    Observable<User> getUserDetails(User u) {
        return Observable.empty();
    }
    
    @Test
    public void test() {
        Observable<ArrayList<User>> users = Observable.just(new ArrayList<>());

                users.flatMapIterable(list -> list)
                     .flatMap(user -> getUserDetails(user))
                     .subscribe(user -> { /* ... */ }, Throwable::printStackTrace);
    }
}
