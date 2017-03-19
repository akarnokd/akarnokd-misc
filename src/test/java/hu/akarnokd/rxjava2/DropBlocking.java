package hu.akarnokd.rxjava2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.*;

import io.reactivex.*;

public class DropBlocking {
    @SuppressWarnings("unchecked")
    public static <T> Subscriber<T> mockSubscriber() {
        Subscriber<T> w = mock(Subscriber.class);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock a) throws Throwable {
                Subscription s = a.getArgument(0);
                s.request(Long.MAX_VALUE);
                return null;
            }
        }).when(w).onSubscribe((Subscription)any());

        return w;
    }

    @Test
    @Ignore
    public void testCreateBackpressureDrop() {
        Subscriber<Integer> w = mockSubscriber();
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(3);
                e.onNext(4);
                e.onComplete();
            }
        }, BackpressureStrategy.DROP).blockingSubscribe(w);

        verify(w).onSubscribe(any());
        verify(w, times(1)).onNext(1);
        verify(w, times(1)).onNext(3);
        verify(w, times(1)).onNext(4);
        verify(w, times(1)).onComplete();
    }
}
