package hu.akarnokd.retrofit2;

import java.lang.reflect.Constructor;

import org.junit.*;

import io.reactivex.Observable;
import retrofit2.Response;

public class ResponseBodyNull {

@Test
public void responseBodyNull() throws Exception {
    
    Response<Integer> response = Response.success(null);
    
    Assert.assertNull(response.body());
    
    Class<?> clazz = Class.forName("retrofit2.adapter.rxjava2.BodyObservable");
    Constructor<?> c = clazz.getDeclaredConstructor(Observable.class);
    c.setAccessible(true);
    @SuppressWarnings({ "unchecked", "rawtypes" })
    Observable<Integer> o = (Observable)c.newInstance(Observable.just(response));
    o.test()
    .assertNever(r -> r == null)
    .assertTerminated();
}
}
